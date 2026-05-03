package fpt.edu.sep490.pilahub.service.implement;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import fpt.edu.sep490.pilahub.enums.OrderDetailStatus;
import fpt.edu.sep490.pilahub.enums.OrderStatus;
import fpt.edu.sep490.pilahub.pojo.Order;
import fpt.edu.sep490.pilahub.pojo.OrderDetail;
import fpt.edu.sep490.pilahub.pojo.Vendor;
import fpt.edu.sep490.pilahub.repository.OrderDetailRepository;
import fpt.edu.sep490.pilahub.repository.OrderRepository;
import fpt.edu.sep490.pilahub.repository.VendorRepository;
import fpt.edu.sep490.pilahub.service.OrderReportService;
import fpt.edu.sep490.pilahub.service.SystemConfigService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderReportServiceImpl implements OrderReportService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final VendorRepository vendorRepository;
    private final EntityManager entityManager;
    private final SystemConfigService systemConfigService;

    // ========== REVENUE REPORTS ==========

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenueByDateRange(Instant startDate, Instant endDate) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<Order> root = query.from(Order.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("paid"), true));
        predicates.add(cb.notEqual(root.get("status"), OrderStatus.CANCELLED));

        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
        }

        query.select(cb.sum(root.get("totalAmount")));
        query.where(predicates.toArray(new Predicate[0]));

        BigDecimal result = entityManager.createQuery(query).getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getDailyRevenue(Instant startDate, Instant endDate) {
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.isPaid() && o.getCreatedAt().isAfter(startDate) && o.getCreatedAt().isBefore(endDate))
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());

        Map<String, BigDecimal> dailyRevenue = new TreeMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Order order : orders) {
            LocalDate date = LocalDateTime.ofInstant(order.getCreatedAt(), ZoneId.systemDefault()).toLocalDate();
            String dateKey = date.format(formatter);
            dailyRevenue.merge(dateKey, order.getTotalAmount(), BigDecimal::add);
        }

        return dailyRevenue;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getMonthlyRevenue(Integer year) {
        Instant startDate = LocalDate.of(year, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endDate = LocalDate.of(year, 12, 31).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.isPaid() && o.getCreatedAt().isAfter(startDate) && o.getCreatedAt().isBefore(endDate))
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());

        Map<String, BigDecimal> monthlyRevenue = new TreeMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (Order order : orders) {
            YearMonth yearMonth = YearMonth.from(LocalDateTime.ofInstant(order.getCreatedAt(), ZoneId.systemDefault()));
            String monthKey = yearMonth.format(formatter);
            monthlyRevenue.merge(monthKey, order.getTotalAmount(), BigDecimal::add);
        }

        return monthlyRevenue;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getVendorRevenueByDateRange(UUID vendorId, Instant startDate, Instant endDate) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<OrderDetail> root = query.from(OrderDetail.class);
        Join<OrderDetail, Order> orderJoin = root.join("order");

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("product").get("vendor").get("vendorId"), vendorId));
        predicates.add(cb.equal(orderJoin.get("paid"), true));
        predicates.add(cb.notEqual(orderJoin.get("status"), OrderStatus.CANCELLED));

        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(orderJoin.get("createdAt"), startDate));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(orderJoin.get("createdAt"), endDate));
        }

        query.select(cb.sum(root.get("subtotal")));
        query.where(predicates.toArray(new Predicate[0]));

        BigDecimal result = entityManager.createQuery(query).getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getVendorNetRevenueByDateRange(UUID vendorId, Instant startDate, Instant endDate) {
        Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
        double feeRate = (vendor != null && vendor.getPlatformFeePercentage() != null)
                ? vendor.getPlatformFeePercentage()
                : systemConfigService.getDefaultPlatformFeePercentage();

        List<OrderDetail> details = vendorDetailStream(vendorId, startDate, endDate);
        BigDecimal net = BigDecimal.ZERO;
        for (OrderDetail od : details) {
            BigDecimal gross = od.getSubtotal();
            BigDecimal platformFee = gross.multiply(BigDecimal.valueOf(feeRate / 100.0));
            net = net.add(gross.subtract(platformFee));
        }
        return net;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getVendorDailyRevenue(UUID vendorId, Instant startDate, Instant endDate) {
        List<OrderDetail> orderDetails = vendorDetailStream(vendorId, startDate, endDate);

        Map<String, BigDecimal> dailyRevenue = new TreeMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (OrderDetail od : orderDetails) {
            LocalDate date = LocalDateTime.ofInstant(od.getOrder().getCreatedAt(), ZoneId.systemDefault())
                    .toLocalDate();
            String dateKey = date.format(formatter);
            dailyRevenue.merge(dateKey, od.getSubtotal(), BigDecimal::add);
        }

        return dailyRevenue;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getVendorMonthlyRevenue(UUID vendorId, Integer year) {
        Instant startDate = LocalDate.of(year, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endDate = LocalDate.of(year, 12, 31).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        List<OrderDetail> orderDetails = vendorDetailStream(vendorId, startDate, endDate);

        Map<String, BigDecimal> monthlyRevenue = new TreeMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (OrderDetail od : orderDetails) {
            YearMonth yearMonth = YearMonth
                    .from(LocalDateTime.ofInstant(od.getOrder().getCreatedAt(), ZoneId.systemDefault()));
            String monthKey = yearMonth.format(formatter);
            monthlyRevenue.merge(monthKey, od.getSubtotal(), BigDecimal::add);
        }

        return monthlyRevenue;
    }

    // ========== ORDER STATISTICS ==========

    @Override
    @Transactional(readOnly = true)
    public Long getOrdersCountByDateRange(Instant startDate, Instant endDate) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Order> root = query.from(Order.class);

        List<Predicate> predicates = new ArrayList<>();
        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
        }

        query.select(cb.count(root));
        query.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<OrderStatus, Long> getOrderStatusDistribution(Instant startDate, Instant endDate) {
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getCreatedAt().isAfter(startDate) && o.getCreatedAt().isBefore(endDate))
                .collect(Collectors.toList());

        return orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
    }

    @Override
    @Transactional(readOnly = true)
    public Long getVendorOrdersCountByDateRange(UUID vendorId, Instant startDate, Instant endDate) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<OrderDetail> root = query.from(OrderDetail.class);
        Join<OrderDetail, Order> orderJoin = root.join("order");

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("product").get("vendor").get("vendorId"), vendorId));

        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(orderJoin.get("createdAt"), startDate));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(orderJoin.get("createdAt"), endDate));
        }

        query.select(cb.countDistinct(orderJoin.get("orderId")));
        query.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<OrderStatus, Long> getVendorOrderStatusDistribution(UUID vendorId, Instant startDate, Instant endDate) {
        List<OrderDetail> orderDetails = orderDetailRepository.findAll().stream()
                .filter(od -> od.getProduct() != null && od.getProduct().getVendor() != null)
                .filter(od -> od.getProduct().getVendor().getVendorId().equals(vendorId))
                .filter(od -> od.getOrder().getCreatedAt().isAfter(startDate) &&
                        od.getOrder().getCreatedAt().isBefore(endDate))
                .collect(Collectors.toList());

        // Distribution is per OrderDetail status (item-level), not order-level
        return orderDetails.stream()
                .collect(Collectors.groupingBy(
                        od -> orderDetailStatusToOrderStatus(od.getStatus()),
                        Collectors.counting()));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getVendorAverageOrderValue(UUID vendorId, Instant startDate, Instant endDate) {
        // Average based on gross revenue (subtotal - shippingFee) per order
        BigDecimal totalGross = getVendorRevenueByDateRange(vendorId, startDate, endDate);
        Long orderCount = getVendorOrdersCountByDateRange(vendorId, startDate, endDate);

        if (orderCount == 0) {
            return BigDecimal.ZERO;
        }

        return totalGross.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP);
    }

    // ========== CUSTOMER ANALYTICS ==========

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopCustomersByRevenue(Instant startDate, Instant endDate, Integer limit) {
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.isPaid() && o.getCreatedAt().isAfter(startDate) && o.getCreatedAt().isBefore(endDate))
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());

        Map<UUID, BigDecimal> customerRevenue = new HashMap<>();
        Map<UUID, String> customerNames = new HashMap<>();
        Map<UUID, Long> orderCounts = new HashMap<>();

        for (Order order : orders) {
            UUID accountId = order.getAccount().getAccountId();
            customerRevenue.merge(accountId, order.getTotalAmount(), BigDecimal::add);
            customerNames.putIfAbsent(accountId, order.getAccount().getEmail());
            orderCounts.merge(accountId, 1L, Long::sum);
        }

        return customerRevenue.entrySet().stream()
                .sorted(Map.Entry.<UUID, BigDecimal>comparingByValue().reversed())
                .limit(limit != null ? limit : 10)
                .map(entry -> {
                    Map<String, Object> customer = new HashMap<>();
                    customer.put("accountId", entry.getKey());
                    customer.put("customerName", customerNames.get(entry.getKey()));
                    customer.put("totalRevenue", entry.getValue());
                    customer.put("orderCount", orderCounts.get(entry.getKey()));
                    return customer;
                })
                .collect(Collectors.toList());
    }

    // ========== PRODUCT ANALYTICS ==========

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMostOrderedProducts(Instant startDate, Instant endDate, Integer limit) {
        List<OrderDetail> orderDetails = orderDetailRepository.findAll().stream()
                .filter(od -> od.getOrder().getCreatedAt().isAfter(startDate) &&
                        od.getOrder().getCreatedAt().isBefore(endDate))
                .filter(od -> od.getOrder().getStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());

        Map<UUID, Integer> productQuantities = new HashMap<>();
        Map<UUID, String> productNames = new HashMap<>();
        Map<UUID, BigDecimal> productRevenue = new HashMap<>();

        for (OrderDetail od : orderDetails) {
            UUID productId = od.getProduct().getProductId();
            productQuantities.merge(productId, od.getQuantity(), Integer::sum);
            productNames.putIfAbsent(productId, od.getProductName());
            productRevenue.merge(productId, od.getSubtotal(), BigDecimal::add);
        }

        return productQuantities.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(limit != null ? limit : 10)
                .map(entry -> {
                    Map<String, Object> product = new HashMap<>();
                    product.put("productId", entry.getKey());
                    product.put("productName", productNames.get(entry.getKey()));
                    product.put("totalQuantity", entry.getValue());
                    product.put("totalRevenue", productRevenue.get(entry.getKey()));
                    return product;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getVendorMostOrderedProducts(UUID vendorId, Instant startDate, Instant endDate,
            Integer limit) {
        List<OrderDetail> orderDetails = orderDetailRepository.findAll().stream()
                .filter(od -> od.getProduct().getVendor().getVendorId().equals(vendorId))
                .filter(od -> od.getOrder().getCreatedAt().isAfter(startDate) &&
                        od.getOrder().getCreatedAt().isBefore(endDate))
                .filter(od -> od.getOrder().getStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());

        Map<UUID, Integer> productQuantities = new HashMap<>();
        Map<UUID, String> productNames = new HashMap<>();
        Map<UUID, BigDecimal> productRevenue = new HashMap<>();

        for (OrderDetail od : orderDetails) {
            UUID productId = od.getProduct().getProductId();
            productQuantities.merge(productId, od.getQuantity(), Integer::sum);
            productNames.putIfAbsent(productId, od.getProductName());
            productRevenue.merge(productId, od.getSubtotal(), BigDecimal::add);
        }

        return productQuantities.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(limit != null ? limit : 10)
                .map(entry -> {
                    Map<String, Object> product = new HashMap<>();
                    product.put("productId", entry.getKey());
                    product.put("productName", productNames.get(entry.getKey()));
                    product.put("totalQuantity", entry.getValue());
                    product.put("totalRevenue", productRevenue.get(entry.getKey()));
                    return product;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getVendorProductPerformance(UUID vendorId, Instant startDate, Instant endDate) {
        Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
        double feeRate = (vendor != null && vendor.getPlatformFeePercentage() != null)
                ? vendor.getPlatformFeePercentage()
                : systemConfigService.getDefaultPlatformFeePercentage();

        List<OrderDetail> orderDetails = vendorDetailStream(vendorId, startDate, endDate);

        Map<UUID, Integer> productQuantities = new HashMap<>();
        Map<UUID, String> productNames = new HashMap<>();
        Map<UUID, BigDecimal> productGrossRevenue = new HashMap<>();
        Map<UUID, BigDecimal> productNetRevenue = new HashMap<>();
        Map<UUID, Long> orderCounts = new HashMap<>();

        for (OrderDetail od : orderDetails) {
            UUID productId = od.getProduct().getProductId();
            BigDecimal gross = od.getSubtotal();
            BigDecimal platformFee = gross.multiply(BigDecimal.valueOf(feeRate / 100.0));
            BigDecimal net = gross.subtract(platformFee);

            productQuantities.merge(productId, od.getQuantity(), Integer::sum);
            productNames.putIfAbsent(productId, od.getProductName());
            productGrossRevenue.merge(productId, gross, BigDecimal::add);
            productNetRevenue.merge(productId, net, BigDecimal::add);
            orderCounts.merge(productId, 1L, Long::sum);
        }

        return productGrossRevenue.entrySet().stream()
                .sorted(Map.Entry.<UUID, BigDecimal>comparingByValue().reversed())
                .map(entry -> {
                    UUID pid = entry.getKey();
                    Map<String, Object> product = new HashMap<>();
                    product.put("productId", pid);
                    product.put("productName", productNames.get(pid));
                    product.put("totalQuantitySold", productQuantities.get(pid));
                    product.put("grossRevenue", entry.getValue());
                    product.put("netRevenue", productNetRevenue.get(pid));
                    product.put("orderCount", orderCounts.get(pid));
                    return product;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getVendorTopProductsByRevenue(UUID vendorId, Instant startDate, Instant endDate,
            Integer limit) {
        List<Map<String, Object>> performance = getVendorProductPerformance(vendorId, startDate, endDate);
        return performance.stream()
                .limit(limit != null ? limit : 10)
                .collect(Collectors.toList());
    }

    // ========== EXPORT REPORTS ==========

    @Override
    @Transactional(readOnly = true)
    public byte[] generateSalesReportPDF(Instant startDate, Instant endDate) {
        log.info("Generating sales report PDF for period: {} to {}", startDate, endDate);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Title
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Sales Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Report period
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.systemDefault());
            Paragraph period = new Paragraph(
                    "Period: " + formatter.format(startDate) + " to " + formatter.format(endDate));
            period.setSpacingAfter(10);
            document.add(period);

            // Summary metrics
            BigDecimal totalRevenue = getTotalRevenueByDateRange(startDate, endDate);
            Long totalOrders = getOrdersCountByDateRange(startDate, endDate);

            Paragraph summary = new Paragraph();
            summary.add(new Chunk("Total Revenue: $" + totalRevenue.toString() + "\n"));
            summary.add(new Chunk("Total Orders: " + totalOrders + "\n"));
            summary.setSpacingAfter(20);
            document.add(summary);

            // Orders table
            List<Order> orders = orderRepository.findAll().stream()
                    .filter(o -> o.getCreatedAt().isAfter(startDate) && o.getCreatedAt().isBefore(endDate))
                    .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                    .collect(Collectors.toList());

            if (!orders.isEmpty()) {
                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10);

                // Header
                Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD);
                addTableHeader(table, headerFont, "Order Number", "Customer", "Date", "Status", "Amount", "Paid");

                // Data rows
                Font cellFont = new Font(Font.HELVETICA, 9);
                for (Order order : orders) {
                    addTableCell(table, cellFont, order.getOrderNumber());
                    addTableCell(table, cellFont, order.getRecipientName());
                    addTableCell(table, cellFont, formatter.format(order.getCreatedAt()));
                    addTableCell(table, cellFont, order.getStatus().toString());
                    addTableCell(table, cellFont, "$" + order.getTotalAmount().toString());
                    addTableCell(table, cellFont, order.isPaid() ? "Yes" : "No");
                }

                document.add(table);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating sales report PDF", e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateSalesReportExcel(Instant startDate, Instant endDate) {
        log.info("Generating sales report Excel for period: {} to {}", startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sales Report");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.systemDefault());

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Summary section
            int rowNum = 0;
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Sales Report");
            titleCell.setCellStyle(headerStyle);

            Row periodRow = sheet.createRow(rowNum++);
            periodRow.createCell(0).setCellValue("Period:");
            periodRow.createCell(1).setCellValue(formatter.format(startDate) + " to " + formatter.format(endDate));

            BigDecimal totalRevenue = getTotalRevenueByDateRange(startDate, endDate);
            Long totalOrders = getOrdersCountByDateRange(startDate, endDate);

            Row revenueRow = sheet.createRow(rowNum++);
            revenueRow.createCell(0).setCellValue("Total Revenue:");
            revenueRow.createCell(1).setCellValue("$" + totalRevenue.toString());

            Row ordersRow = sheet.createRow(rowNum++);
            ordersRow.createCell(0).setCellValue("Total Orders:");
            ordersRow.createCell(1).setCellValue(totalOrders);

            rowNum++; // Empty row

            // Orders table header
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = { "Order Number", "Customer", "Email", "Date", "Status", "Payment Method", "Amount",
                    "Shipping Fee", "Paid" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Orders data
            List<Order> orders = orderRepository.findAll().stream()
                    .filter(o -> o.getCreatedAt().isAfter(startDate) && o.getCreatedAt().isBefore(endDate))
                    .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                    .collect(Collectors.toList());

            for (Order order : orders) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(order.getOrderNumber());
                row.createCell(1).setCellValue(order.getRecipientName());
                row.createCell(2).setCellValue(order.getAccount().getEmail());
                row.createCell(3).setCellValue(formatter.format(order.getCreatedAt()));
                row.createCell(4).setCellValue(order.getStatus().toString());
                row.createCell(5).setCellValue(order.getPaymentMethod());
                row.createCell(6).setCellValue(order.getTotalAmount().doubleValue());
                row.createCell(7)
                        .setCellValue(order.getShippingFee() != null ? order.getShippingFee().doubleValue() : 0.0);
                row.createCell(8).setCellValue(order.isPaid() ? "Yes" : "No");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error generating sales report Excel", e);
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportOrdersToCSV(Instant startDate, Instant endDate) {
        log.info("Exporting orders to CSV for period: {} to {}", startDate, endDate);

        StringBuilder csv = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault());

        // CSV Header
        csv.append("Order Number,Customer Name,Customer Email,Recipient Phone,Shipping Address,")
                .append("Date,Status,Payment Method,Total Amount,Shipping Fee,Discount,Paid\n");

        // Get orders
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getCreatedAt().isAfter(startDate) && o.getCreatedAt().isBefore(endDate))
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());

        // CSV Data rows
        for (Order order : orders) {
            csv.append(escapeCsv(order.getOrderNumber())).append(",")
                    .append(escapeCsv(order.getRecipientName())).append(",")
                    .append(escapeCsv(order.getAccount().getEmail())).append(",")
                    .append(escapeCsv(order.getRecipientPhone())).append(",")
                    .append(escapeCsv(order.getShippingAddress())).append(",")
                    .append(formatter.format(order.getCreatedAt())).append(",")
                    .append(order.getStatus().toString()).append(",")
                    .append(escapeCsv(order.getPaymentMethod())).append(",")
                    .append(order.getTotalAmount().toString()).append(",")
                    .append(order.getShippingFee() != null ? order.getShippingFee().toString() : "0").append(",")
                    .append(order.getDiscountAmount().toString()).append(",")
                    .append(order.isPaid() ? "Yes" : "No").append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateVendorSalesReportPDF(UUID vendorId, Instant startDate, Instant endDate) {
        log.info("Generating vendor sales report PDF for vendor: {}", vendorId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Title
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Vendor Sales Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Report period
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.systemDefault());
            Paragraph period = new Paragraph(
                    "Period: " + formatter.format(startDate) + " to " + formatter.format(endDate));
            period.setSpacingAfter(10);
            document.add(period);

            // Summary metrics
            BigDecimal totalGross = getVendorRevenueByDateRange(vendorId, startDate, endDate);
            BigDecimal totalNet = getVendorNetRevenueByDateRange(vendorId, startDate, endDate);
            Long totalOrders = getVendorOrdersCountByDateRange(vendorId, startDate, endDate);
            BigDecimal avgOrderValue = getVendorAverageOrderValue(vendorId, startDate, endDate);

            Paragraph summary = new Paragraph();
            summary.add(new Chunk("Gross Revenue: " + totalGross.toString() + " VND\n"));
            summary.add(new Chunk("Net Revenue: " + totalNet.toString() + " VND\n"));
            summary.add(new Chunk("Total Orders: " + totalOrders + "\n"));
            summary.add(new Chunk("Average Order Value (Gross): " + avgOrderValue.toString() + " VND\n"));
            summary.setSpacingAfter(20);
            document.add(summary);

            // Top products
            List<Map<String, Object>> topProducts = getVendorMostOrderedProducts(vendorId, startDate, endDate, 10);

            if (!topProducts.isEmpty()) {
                Paragraph productsTitle = new Paragraph("Top Products", new Font(Font.HELVETICA, 14, Font.BOLD));
                productsTitle.setSpacingAfter(10);
                document.add(productsTitle);

                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10);

                Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD);
                addTableHeader(table, headerFont, "Product Name", "Quantity Sold", "Revenue", "Orders");

                Font cellFont = new Font(Font.HELVETICA, 9);
                for (Map<String, Object> product : topProducts) {
                    addTableCell(table, cellFont, product.get("productName").toString());
                    addTableCell(table, cellFont, product.get("totalQuantity").toString());
                    addTableCell(table, cellFont, "$" + product.get("totalRevenue").toString());
                    addTableCell(table, cellFont, "-");
                }

                document.add(table);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating vendor sales report PDF", e);
            throw new RuntimeException("Failed to generate vendor PDF report", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateVendorSalesReportExcel(UUID vendorId, Instant startDate, Instant endDate) {
        log.info("Generating vendor sales report Excel for vendor: {}", vendorId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.systemDefault());

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Summary sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            int rowNum = 0;

            Row titleRow = summarySheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Vendor Sales Report");
            titleCell.setCellStyle(headerStyle);

            Row periodRow = summarySheet.createRow(rowNum++);
            periodRow.createCell(0).setCellValue("Period:");
            periodRow.createCell(1).setCellValue(formatter.format(startDate) + " to " + formatter.format(endDate));

            BigDecimal totalGross = getVendorRevenueByDateRange(vendorId, startDate, endDate);
            BigDecimal totalNet = getVendorNetRevenueByDateRange(vendorId, startDate, endDate);
            Long totalOrders = getVendorOrdersCountByDateRange(vendorId, startDate, endDate);
            BigDecimal avgOrderValue = getVendorAverageOrderValue(vendorId, startDate, endDate);

            Row grossRow = summarySheet.createRow(rowNum++);
            grossRow.createCell(0).setCellValue("Gross Revenue (VND):");
            grossRow.createCell(1).setCellValue(totalGross.doubleValue());

            Row netRow = summarySheet.createRow(rowNum++);
            netRow.createCell(0).setCellValue("Net Revenue (VND):");
            netRow.createCell(1).setCellValue(totalNet.doubleValue());

            Row ordersRow = summarySheet.createRow(rowNum++);
            ordersRow.createCell(0).setCellValue("Total Orders:");
            ordersRow.createCell(1).setCellValue(totalOrders);

            Row avgRow = summarySheet.createRow(rowNum++);
            avgRow.createCell(0).setCellValue("Avg Order Value - Gross (VND):");
            avgRow.createCell(1).setCellValue(avgOrderValue.doubleValue());

            // Products sheet – per-product gross & net
            Sheet productsSheet = workbook.createSheet("Product Performance");
            rowNum = 0;

            Row headerRow = productsSheet.createRow(rowNum++);
            String[] headers = { "Product Name", "Qty Sold", "Order Count", "Gross Revenue (VND)",
                    "Net Revenue (VND)" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            List<Map<String, Object>> productPerf = getVendorProductPerformance(vendorId, startDate, endDate);
            for (Map<String, Object> product : productPerf) {
                Row row = productsSheet.createRow(rowNum++);
                row.createCell(0)
                        .setCellValue(product.get("productName") != null ? product.get("productName").toString() : "");
                row.createCell(1).setCellValue(Integer.parseInt(product.get("totalQuantitySold").toString()));
                row.createCell(2).setCellValue(Long.parseLong(product.get("orderCount").toString()));
                row.createCell(3).setCellValue(Double.parseDouble(product.get("grossRevenue").toString()));
                row.createCell(4).setCellValue(Double.parseDouble(product.get("netRevenue").toString()));
            }

            // Order details sheet
            Sheet detailsSheet = workbook.createSheet("Order Details");
            rowNum = 0;

            Row detailHeaderRow = detailsSheet.createRow(rowNum++);
            String[] detailHeaders = { "Order Number", "Order Date", "Order Status",
                    "Item Status", "Product Name", "Qty", "Unit Price (VND)",
                    "Gross/Subtotal (VND)", "Platform Fee (VND)", "Net (VND)" };
            for (int i = 0; i < detailHeaders.length; i++) {
                Cell cell = detailHeaderRow.createCell(i);
                cell.setCellValue(detailHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
            double feeRate = (vendor != null && vendor.getPlatformFeePercentage() != null)
                    ? vendor.getPlatformFeePercentage()
                    : systemConfigService.getDefaultPlatformFeePercentage();

            DateTimeFormatter detailFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.systemDefault());

            List<OrderDetail> allDetails = vendorDetailStream(vendorId, startDate, endDate);
            for (OrderDetail od : allDetails) {
                BigDecimal gross = od.getSubtotal();
                BigDecimal fee = gross.multiply(BigDecimal.valueOf(feeRate / 100.0));
                BigDecimal net = gross.subtract(fee);

                Row row = detailsSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(od.getOrder().getOrderNumber());
                row.createCell(1).setCellValue(detailFormatter.format(od.getOrder().getCreatedAt()));
                row.createCell(2).setCellValue(od.getOrder().getStatus().toString());
                row.createCell(3).setCellValue(od.getStatus().toString());
                row.createCell(4).setCellValue(od.getProductName() != null ? od.getProductName() : "");
                row.createCell(5).setCellValue(od.getQuantity());
                row.createCell(6).setCellValue(od.getUnitPrice().doubleValue());
                row.createCell(7).setCellValue(od.getSubtotal().doubleValue());
                row.createCell(8).setCellValue(fee.doubleValue());
                row.createCell(9).setCellValue(net.doubleValue());
            }

            // Auto-size all sheets
            for (int i = 0; i < headers.length; i++)
                productsSheet.autoSizeColumn(i);
            for (int i = 0; i < detailHeaders.length; i++)
                detailsSheet.autoSizeColumn(i);
            summarySheet.autoSizeColumn(0);
            summarySheet.autoSizeColumn(1);

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error generating vendor sales report Excel", e);
            throw new RuntimeException("Failed to generate vendor Excel report", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportVendorOrdersToCSV(UUID vendorId, Instant startDate, Instant endDate) {
        log.info("Exporting vendor orders to CSV for vendor: {}", vendorId);

        StringBuilder csv = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault());

        // CSV Header
        csv.append("Product Name,Product ID,Quantity,Unit Price,Subtotal,Discount,")
                .append("Order Number,Customer Name,Order Date,Order Status\n");

        // Get vendor order details
        List<OrderDetail> orderDetails = orderDetailRepository.findAll().stream()
                .filter(od -> od.getProduct().getVendor().getVendorId().equals(vendorId))
                .filter(od -> od.getOrder().getCreatedAt().isAfter(startDate) &&
                        od.getOrder().getCreatedAt().isBefore(endDate))
                .sorted(Comparator.comparing(od -> od.getOrder().getCreatedAt(), Comparator.reverseOrder()))
                .collect(Collectors.toList());

        // CSV Data rows
        for (OrderDetail od : orderDetails) {
            Order order = od.getOrder();
            csv.append(escapeCsv(od.getProductName())).append(",")
                    .append(od.getProduct().getProductId().toString()).append(",")
                    .append(od.getQuantity()).append(",")
                    .append(od.getUnitPrice().toString()).append(",")
                    .append(od.getSubtotal().toString()).append(",")
                    .append(od.getDiscountAmount().toString()).append(",")
                    .append(escapeCsv(order.getOrderNumber())).append(",")
                    .append(escapeCsv(order.getRecipientName())).append(",")
                    .append(formatter.format(order.getCreatedAt())).append(",")
                    .append(order.getStatus().toString()).append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ========== DASHBOARD METRICS ==========

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardMetrics(Instant startDate, Instant endDate) {
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("totalRevenue", getTotalRevenueByDateRange(startDate, endDate));
        metrics.put("totalOrders", getOrdersCountByDateRange(startDate, endDate));
        metrics.put("dailyOrders", getDailyOrders(startDate, endDate));
        metrics.put("orderStatusDistribution", getOrderStatusDistribution(startDate, endDate));
        metrics.put("topCustomers", getTopCustomersByRevenue(startDate, endDate, 5));
        metrics.put("topProducts", getMostOrderedProducts(startDate, endDate, 5));
        metrics.put("dailyRevenue", getDailyRevenue(startDate, endDate));

        return metrics;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getVendorDashboardMetrics(UUID vendorId, Instant startDate, Instant endDate) {
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("grossRevenue", getVendorRevenueByDateRange(vendorId, startDate, endDate));
        metrics.put("netRevenue", getVendorNetRevenueByDateRange(vendorId, startDate, endDate));
        metrics.put("totalOrders", getVendorOrdersCountByDateRange(vendorId, startDate, endDate));
        metrics.put("averageOrderValue", getVendorAverageOrderValue(vendorId, startDate, endDate));
        metrics.put("orderStatusDistribution", getVendorOrderStatusDistribution(vendorId, startDate, endDate));
        metrics.put("topProducts", getVendorMostOrderedProducts(vendorId, startDate, endDate, 5));
        metrics.put("productPerformance", getVendorProductPerformance(vendorId, startDate, endDate));
        metrics.put("dailyOrders", getVendorDailyOrders(vendorId, startDate, endDate));
        metrics.put("dailyRevenue", getVendorDailyRevenue(vendorId, startDate, endDate));

        return metrics;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getVendorPerformanceComparison(UUID vendorId, Instant currentStart, Instant currentEnd,
            Instant previousStart, Instant previousEnd) {
        Map<String, Object> comparison = new HashMap<>();

        BigDecimal currentGross = getVendorRevenueByDateRange(vendorId, currentStart, currentEnd);
        BigDecimal previousGross = getVendorRevenueByDateRange(vendorId, previousStart, previousEnd);
        BigDecimal currentNet = getVendorNetRevenueByDateRange(vendorId, currentStart, currentEnd);
        BigDecimal previousNet = getVendorNetRevenueByDateRange(vendorId, previousStart, previousEnd);

        Long currentOrders = getVendorOrdersCountByDateRange(vendorId, currentStart, currentEnd);
        Long previousOrders = getVendorOrdersCountByDateRange(vendorId, previousStart, previousEnd);

        comparison.put("currentPeriod", Map.of(
                "grossRevenue", currentGross,
                "netRevenue", currentNet,
                "orders", currentOrders));

        comparison.put("previousPeriod", Map.of(
                "grossRevenue", previousGross,
                "netRevenue", previousNet,
                "orders", previousOrders));

        // Calculate growth rates (gross)
        if (previousGross.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal revenueGrowth = currentGross.subtract(previousGross)
                    .divide(previousGross, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            comparison.put("grossRevenueGrowthPercentage", revenueGrowth);
        }

        // Calculate growth rates (net)
        if (previousNet.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal netGrowth = currentNet.subtract(previousNet)
                    .divide(previousNet, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            comparison.put("netRevenueGrowthPercentage", netGrowth);
        }

        if (previousOrders > 0) {
            double orderGrowth = ((double) (currentOrders - previousOrders) / previousOrders) * 100;
            comparison.put("orderGrowthPercentage", orderGrowth);
        }

        return comparison;
    }

    // ========== HELPER METHODS ==========

    /**
     * Shared stream of vendor OrderDetails within a date range, only paid and
     * non-cancelled.
     * gross per item = subtotal
     * net per item = gross - platformFee (platformFee = gross *
     * vendor.platformFeePercentage / 100)
     */
    private List<OrderDetail> vendorDetailStream(UUID vendorId, Instant startDate, Instant endDate) {
        return orderDetailRepository.findAll().stream()
                .filter(od -> od.getProduct() != null && od.getProduct().getVendor() != null)
                .filter(od -> od.getProduct().getVendor().getVendorId().equals(vendorId))
                .filter(od -> od.getOrder().isPaid())
                .filter(od -> od.getOrder().getStatus() != OrderStatus.CANCELLED)
                .filter(od -> od.getStatus() != OrderDetailStatus.CANCELLED
                        && od.getStatus() != OrderDetailStatus.RETURNED
                        && od.getStatus() != OrderDetailStatus.REFUNDED)
                .filter(od -> startDate == null || od.getOrder().getCreatedAt().isAfter(startDate))
                .filter(od -> endDate == null || od.getOrder().getCreatedAt().isBefore(endDate))
                .collect(Collectors.toList());
    }

    /**
     * Daily order count for dashboard charting.
     */
    private Map<String, Long> getDailyOrders(Instant startDate, Instant endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return orderRepository.findAll().stream()
                .filter(o -> startDate == null || !o.getCreatedAt().isBefore(startDate))
                .filter(o -> endDate == null || !o.getCreatedAt().isAfter(endDate))
                .collect(Collectors.groupingBy(
                        o -> LocalDateTime.ofInstant(o.getCreatedAt(), ZoneId.systemDefault()).toLocalDate()
                                .format(formatter),
                        TreeMap::new,
                        Collectors.counting()));
    }

    /**
     * Daily distinct order count for a vendor dashboard chart.
     */
    private Map<String, Long> getVendorDailyOrders(UUID vendorId, Instant startDate, Instant endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return vendorDetailStream(vendorId, startDate, endDate).stream()
                .map(OrderDetail::getOrder)
                .collect(Collectors.groupingBy(
                        o -> LocalDateTime.ofInstant(o.getCreatedAt(), ZoneId.systemDefault()).toLocalDate()
                                .format(formatter),
                        TreeMap::new,
                        Collectors.mapping(Order::getOrderId,
                                Collectors.collectingAndThen(Collectors.toSet(), ids -> (long) ids.size()))));
    }

    /**
     * Maps an OrderDetailStatus to the closest OrderStatus bucket for distribution
     * reports.
     */
    private OrderStatus orderDetailStatusToOrderStatus(OrderDetailStatus s) {
        return switch (s) {
            case CANCELLED, OUT_OF_STOCK -> OrderStatus.CANCELLED;
            case PENDING -> OrderStatus.PENDING;
            case CONFIRMED -> OrderStatus.CONFIRMED;
            case READY -> OrderStatus.READY;
            case SHIPPED -> OrderStatus.SHIPPED;
            case DELIVERED -> OrderStatus.DELIVERED;
            case COMPLETED -> OrderStatus.COMPLETED;
            case RETURNED -> OrderStatus.RETURNED;
            case REFUNDED -> OrderStatus.REFUNDED;
        };
    }

    private void addTableHeader(PdfPTable table, Font font, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(new Color(200, 200, 200));
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addTableCell(PdfPTable table, Font font, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String escapeCsv(String value) {
        if (value == null)
            return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
