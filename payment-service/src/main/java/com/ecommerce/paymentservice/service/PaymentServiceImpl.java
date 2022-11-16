package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.entity.TransactionDetails;
import com.ecommerce.paymentservice.model.PaymentMode;
import com.ecommerce.paymentservice.model.PaymentRequest;
import com.ecommerce.paymentservice.model.PaymentResponse;
import com.ecommerce.paymentservice.repository.TransactionDetailsRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Log4j2
public class PaymentServiceImpl implements PaymentService{

    @Autowired
    private TransactionDetailsRepository transactionDetailsRepository;
    @Override
    public long doPayment(PaymentRequest paymentRequest) {
        log.info("Recording payment Details: {}", paymentRequest);

        TransactionDetails transactionDetails = TransactionDetails.builder()
                .paymentDate(Instant.now())
                .paymentMode(paymentRequest.getPaymentMode().name())
                .paymentStatus("SUCCESS")
                .orderId(paymentRequest.getOrderId())
                .referenceNumber(paymentRequest.getReferenceNumber())
                .amount(paymentRequest.getAmount())
                .build();

        transactionDetailsRepository.save(transactionDetails);

        log.info("Transaction completed with Id: {}", transactionDetails);

        return transactionDetails.getId();
    }

    @Override
    public PaymentResponse getPaymentDetailsByOrderId(long orderId) {
        log.info("Payment details for the orderId: {}", orderId);
        TransactionDetails transactionDetails = transactionDetailsRepository.findByOrderId(orderId);
        log.info("transactionDetails: {}", transactionDetails);
        PaymentResponse paymentResponse = PaymentResponse.builder()
                .paymentId(transactionDetails.getId())
                .status(transactionDetails.getPaymentStatus())
                .PaymentDate(transactionDetails.getPaymentDate())
                .orderId(transactionDetails.getOrderId())
                .amount(transactionDetails.getAmount())
                .paymentMode(PaymentMode.valueOf(transactionDetails.getPaymentMode()))
                .build();
        return paymentResponse;
    }
}
