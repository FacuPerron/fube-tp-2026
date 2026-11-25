package com.tiendaonline.pedidos.batch;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.tiendaonline.pedidos.model.EstadoPedido;
import com.tiendaonline.pedidos.model.Pedido;
import com.tiendaonline.pedidos.repository.PedidoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ExpiracionPedidosJobConfig {

    private final PedidoRepository pedidoRepository;
    private final ExpiracionPedidosProcessor processor;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    // Busca todos los pedidos "CREADO" hace más de 24hs
    @Bean
    @StepScope
    public ItemReader<Pedido> expirarPedidosReader() {
        LocalDateTime fechaLimite = LocalDateTime.now().minusHours(24);
        List<Pedido> pedidos = pedidoRepository.findByEstadoAndFechaCreacionBefore(EstadoPedido.CREADO, fechaLimite);
        return new ListItemReader<>(pedidos);
    }

    /*
     * Leemos los pedidos, liberamos sus reservas,
     * actualizamos el estado y guardamos los cambios
     */
    @Bean
    public Step expirarPedidosStep(ItemReader<Pedido> expirarPedidosReader) {
        return new StepBuilder("expirarPedidosStep", jobRepository)
                .<Pedido, Pedido>chunk(10, transactionManager)
                .reader(expirarPedidosReader)
                .processor(processor)
                .writer(chunk -> pedidoRepository.saveAll(chunk.getItems()))
                .build();
    }

    // Generamos reporte
    @Bean
    public Job expirarPedidosJob(Step expirarPedidosStep) {
        return new JobBuilder("expirarPedidosJob", jobRepository)
                .start(expirarPedidosStep)
                .listener(new JobExecutionListener() {
                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        long expirables = 0;
                        long expirados = 0;
                        for (StepExecution step : jobExecution.getStepExecutions()) {
                            expirables += step.getReadCount(); // Cantidad de pedidos a expirar
                            expirados += step.getWriteCount(); // Cantidad de pedidos expirados
                        }
                        log.info("Corrida de expiraciones: Estado de ejecución: {}, expirables = {}, expirados = {}",
                                jobExecution.getStatus(), expirables, expirados);
                    }
                })
                .build();
    }
}
