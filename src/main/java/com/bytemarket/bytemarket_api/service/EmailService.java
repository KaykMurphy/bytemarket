package com.bytemarket.bytemarket_api.service;

import com.bytemarket.bytemarket_api.domain.Order;
import com.bytemarket.bytemarket_api.domain.OrderItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${bytemarket.email.from}")
    private String fromEmail;

    @Value("${bytemarket.email.name}")
    private String fromName;

    /**
     * Envia email com as contas digitais compradas
     */
    public void sendOrderConfirmation(Order order, List<List<String>> accountsPerItem) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(order.getDeliveryEmail());
            helper.setSubject("✅ Pedido #" + order.getId() + " Confirmado - ByteMarket");

            // Preparar dados para o template
            Context context = new Context();
            context.setVariable("orderId", order.getId());
            context.setVariable("total", order.getTotal());
            context.setVariable("orderDate", order.getMoment());
            context.setVariable("items", buildItemsData(order.getItems(), accountsPerItem));

            // Renderizar template HTML
            String htmlContent = templateEngine.process("email/order-confirmation", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("Email de confirmação enviado para: {}", order.getDeliveryEmail());

        } catch (Exception e) {
            log.error("Erro ao enviar email de confirmação para pedido {}: {}",
                    order.getId(), e.getMessage());
            // Não lançar exceção para não quebrar o fluxo do pedido
        }
    }

    /**
     * Envia email de notificação de pagamento aprovado
     */
    public void sendPaymentApproved(Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(order.getDeliveryEmail());
            helper.setSubject("💰 Pagamento Aprovado - Pedido #" + order.getId());

            Context context = new Context();
            context.setVariable("orderId", order.getId());
            context.setVariable("total", order.getTotal());

            String htmlContent = templateEngine.process("email/payment-approved", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("Email de pagamento aprovado enviado para: {}", order.getDeliveryEmail());

        } catch (Exception e) {
            log.error("Erro ao enviar email de pagamento aprovado: {}", e.getMessage());
        }
    }

    /**
     * Prepara dados dos itens para o template
     */
    private List<Map<String, Object>> buildItemsData(List<OrderItem> items, List<List<String>> accountsPerItem) {
        return items.stream()
                .map(item -> {
                    int index = items.indexOf(item);
                    List<String> accounts = accountsPerItem.get(index);

                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("productTitle", item.getProduct().getTitle());
                    itemData.put("quantity", item.getQuantity());
                    itemData.put("price", item.getPrice());
                    itemData.put("accounts", accounts);

                    return itemData;
                })
                .collect(Collectors.toList());
    }
}