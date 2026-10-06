package com.pm.billingservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import com.pm.billingservice.model.BillingAccount;
import com.pm.billingservice.repository.BillingAccountRepository;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {
  private final BillingAccountRepository repository;

  public BillingGrpcService(BillingAccountRepository repository) { this.repository = repository; }

  private static final Logger log = LoggerFactory.getLogger(
      BillingGrpcService.class);

  @Override
  @Transactional
  public void createBillingAccount(BillingRequest billingRequest,
      StreamObserver<BillingResponse> responseObserver) {

      log.info("createBillingAccount request received {}", billingRequest.toString());

      BillingAccount account = repository.findByPatientId(billingRequest.getPatientId())
          .orElseGet(BillingAccount::new);
      account.setPatientId(billingRequest.getPatientId());
      account.setPatientName(billingRequest.getName());
      account.setPatientEmail(billingRequest.getEmail());
      account = repository.save(account);
      BillingResponse response = BillingResponse.newBuilder()
          .setAccountId(account.getId().toString())
          .setStatus(account.getStatus())
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
  }
}
