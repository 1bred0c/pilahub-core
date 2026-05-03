package pilahub.service;

import org.springframework.web.multipart.MultipartFile;
import pilahub.dto.request.InBodyBase64ExtractionRequest;
import pilahub.dto.response.InBodyExtractionResponse;

public interface InBodyExtractionService {
    InBodyExtractionResponse extractFromMultipart(MultipartFile imageFile, String rawScanId);

    InBodyExtractionResponse extractFromBase64(InBodyBase64ExtractionRequest request);
}

