package com.avaali.velocious.formrecognizerapp.service;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class FormRecognizer {
    //use your `key` and `endpoint` environment variables
    @Value("${velocious.azure.document.intelligence.key}")
    private static String key = "0864879375b74991a4bbdce8f7e7e314";
    @Value("${velocious.azure.document.intelligence.endpoint}")
    private static String endpoint = "https://invoiceread.cognitiveservices.azure.com/";

    public static void main(final String[] args) throws JsonProcessingException {

        // create your `DocumentAnalysisClient` instance and `AzureKeyCredential` variable
        DocumentAnalysisClient client = new DocumentAnalysisClientBuilder()
                .credential(new AzureKeyCredential(key))
                .endpoint(endpoint)
                .buildClient();

// sample document
        String invoiceUrl = "https://github.com/Azure-Samples/cognitive-services-REST-api-samples/raw/master/curl/form-recognizer/rest-api/invoice.pdf";
        String modelId = "prebuilt-invoice";
        File file = new File("D:/TestFiles/invoice.pdf");
        SyncPoller<OperationResult, AnalyzeResult> analyzeInvoicesPoller =
//                client.beginAnalyzeDocumentFromUrl(modelId, invoiceUrl);
            client.beginAnalyzeDocument(modelId, BinaryData.fromFile(Path.of("D:/TestFiles/TS10_000007CMB21.pdf")));
        AnalyzeResult analyzeInvoiceResult = analyzeInvoicesPoller.getFinalResult();

        for (int i = 0; i < analyzeInvoiceResult.getDocuments().size(); i++) {
            AnalyzedDocument analyzedInvoice = analyzeInvoiceResult.getDocuments().get(i);
            Map< String, DocumentField> invoiceFields = analyzedInvoice.getFields();
            System.out.printf("----------- Analyzing invoice  %d -----------%n", i);
            DocumentField vendorNameField = invoiceFields.get("VendorName");
            if (vendorNameField != null) {
                if (DocumentFieldType.STRING == vendorNameField.getType()) {
                    String merchantName = vendorNameField.getValueAsString();
                    System.out.printf("Vendor Name: %s, confidence: %.2f%n",
                            merchantName, vendorNameField.getConfidence());
                }
            }

            DocumentField vendorAddressField = invoiceFields.get("VendorAddress");
            if (vendorAddressField != null) {
                if (DocumentFieldType.STRING == vendorAddressField.getType()) {
                    String merchantAddress = vendorAddressField.getValueAsString();
                    System.out.printf("Vendor address: %s, confidence: %.2f%n",
                            merchantAddress, vendorAddressField.getConfidence());
                }
            }

            DocumentField customerNameField = invoiceFields.get("CustomerName");
            if (customerNameField != null) {
                if (DocumentFieldType.STRING == customerNameField.getType()) {
                    String merchantAddress = customerNameField.getValueAsString();
                    System.out.printf("Customer Name: %s, confidence: %.2f%n",
                            merchantAddress, customerNameField.getConfidence());
                }
            }

            DocumentField customerAddressRecipientField = invoiceFields.get("CustomerAddressRecipient");
            if (customerAddressRecipientField != null) {
                if (DocumentFieldType.STRING == customerAddressRecipientField.getType()) {
                    String customerAddr = customerAddressRecipientField.getValueAsString();
                    System.out.printf("Customer Address Recipient: %s, confidence: %.2f%n",
                            customerAddr, customerAddressRecipientField.getConfidence());
                }
            }

            DocumentField invoiceIdField = invoiceFields.get("InvoiceId");
            if (invoiceIdField != null) {
                if (DocumentFieldType.STRING == invoiceIdField.getType()) {
                    String invoiceId = invoiceIdField.getValueAsString();
                    System.out.printf("Invoice ID: %s, confidence: %.2f%n",
                            invoiceId, invoiceIdField.getConfidence());
                }
            }

            DocumentField invoiceDateField = invoiceFields.get("InvoiceDate");
            if (customerNameField != null) {
                if (DocumentFieldType.DATE == invoiceDateField.getType()) {
                    LocalDate invoiceDate = invoiceDateField.getValueAsDate();
                    System.out.printf("Invoice Date: %s, confidence: %.2f%n",
                            invoiceDate, invoiceDateField.getConfidence());
                }
            }

            DocumentField invoiceTotalField = invoiceFields.get("InvoiceTotal");
            if (customerAddressRecipientField != null) {
                if (DocumentFieldType.DOUBLE == invoiceTotalField.getType()) {
                    Double invoiceTotal = invoiceTotalField.getValueAsDouble();
                    System.out.printf("Invoice Total: %.2f, confidence: %.2f%n",
                            invoiceTotal, invoiceTotalField.getConfidence());
                }
            }

            DocumentField invoiceItemsField = invoiceFields.get("Items");
            if (invoiceItemsField != null) {
                System.out.printf("Invoice Items: %n");
                if (DocumentFieldType.LIST == invoiceItemsField.getType()) {
                    List< DocumentField > invoiceItems = invoiceItemsField.getValueAsList();
                    invoiceItems.stream()
                            .filter(invoiceItem -> DocumentFieldType.MAP == invoiceItem.getType())
                            .map(documentField -> documentField.getValueAsMap())
                            .forEach(documentFieldMap -> documentFieldMap.forEach((key, documentField) -> {
                                if ("Description".equals(key)) {
                                    if (DocumentFieldType.STRING == documentField.getType()) {
                                        String name = documentField.getValueAsString();
                                        System.out.printf("Description: %s, confidence: %.2fs%n",
                                                name, documentField.getConfidence());
                                    }
                                }
                                if ("Quantity".equals(key)) {
                                    if (DocumentFieldType.DOUBLE == documentField.getType()) {
                                        Double quantity = documentField.getValueAsDouble();
                                        System.out.printf("Quantity: %f, confidence: %.2f%n",
                                                quantity, documentField.getConfidence());
                                    }
                                }
                                if ("UnitPrice".equals(key)) {
                                    if (DocumentFieldType.DOUBLE == documentField.getType()) {
                                        Double unitPrice = documentField.getValueAsDouble();
                                        System.out.printf("Unit Price: %f, confidence: %.2f%n",
                                                unitPrice, documentField.getConfidence());
                                    }
                                }
                                if ("ProductCode".equals(key)) {
                                    if (DocumentFieldType.DOUBLE == documentField.getType()) {
                                        Double productCode = documentField.getValueAsDouble();
                                        System.out.printf("Product Code: %f, confidence: %.2f%n",
                                                productCode, documentField.getConfidence());
                                    }
                                }
                            }));
                }
            }
        }
    }
}
