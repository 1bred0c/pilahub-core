package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.request.CreateDepositRequest;
import fpt.edu.sep490.pilahub.dto.response.MoMoDepositResponse;

import java.util.Map;
import java.util.UUID;

public interface MoMoService {

    MoMoDepositResponse createDepositPayment(UUID accountId, CreateDepositRequest request);

    Map<String, Object> handlePaymentCallback(Map<String, String> params);

    boolean verifyPaymentCallback(Map<String, String> params);
}

