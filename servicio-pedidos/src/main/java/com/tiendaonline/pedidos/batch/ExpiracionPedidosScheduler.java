package com.tiendaonline.pedidos.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiracionPedidosScheduler {

    private final JobLauncher jobLauncher;
    private final Job expirarPedidosJob;

    @Scheduled(cron = "0 0 2 * * *")
    public void ejecutarExpiracionNocturna() {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobLauncher.run(expirarPedidosJob, params);
        } catch (Exception ex) {
            log.error("Error al ejecutar el job de expiraciones: {}", ex.getMessage());
        }
    }
}
