package com.invoiceflow.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI invoiceFlowOpenApi() {
        return new OpenAPI().info(new Info()
                .title("InvoiceFlow API")
                .description("""
                        AI invoice processing pipeline - email in, structured data out.

                        Inbound emails are picked up by an n8n workflow, invoice fields are extracted \
                        by an OpenAI model, validated and stored by this Spring Boot service, and the team \
                        is notified in Slack. Low-confidence extractions are routed to a human review queue. \
                        All vendors and invoices in this instance are fictional sample data.""")
                .version("1.0.0")
                .contact(new Contact().name("InvoiceFlow")));
    }
}
