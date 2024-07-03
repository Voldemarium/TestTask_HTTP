package stepanovvv.ru;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CrptApi {
    private final AtomicLong count = new AtomicLong(0);
    private final AtomicLong countTime = new AtomicLong(0);
    private final long timeLimit;
    private final int requestLimit;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeLimit = timeUnit.toMillis(1L);
        this.requestLimit = requestLimit;
    }

    public synchronized void service() {
        long startTime = System.currentTimeMillis();

        //---------бизнес-логика------------------------
        // Создадим стандартный HTTP клиент
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost requestPost = new HttpPost("https://ismp.crpt.ru/api/v3/lk/documents/create");
        //Формируем данные для отправки на сервер в формате JSON
        HttpResponse response;
        try {
            StringEntity stringEntity = new StringEntity(
                    " {\"description\": { \"participantInn\": \"string\" }, " +
                            "\"doc_id\": \"string\"," +
                            "\"doc_status\": \"string\"," +
                            "\"doc_type\":\"LP_INTRODUCE_GOODS\"," +
                            "109 \"importRequest\":true," +
                            "\"owner_inn\":\"string\"," +
                            "\"participant_inn\":\"string\"," +
                            "\"producer_inn\":\"string\"," +
                            "\"production_date\":\"2020-01-23\"," +
                            "\"production_type\":\"string\"," +
                            "\"products\": " +
                            "[{" +
                            "\"certificate_document\":\"string\"," +
                            "\"certificate_document_date\":\"2020-01-23\"," +
                            "\"certificate_document_number\":\"string\"," +
                            "\"owner_inn\":\"string\"," +
                            "\"producer_inn\":\"string\"," +
                            "\"production_date\":\"2020-01-23\"," +
                            "\"tnved_code\":\"string\"," +
                            "\"uit_code\":\"string\"," +
                            "\"uitu_code\":\"string\"" +
                            "}]," +
                            "\"reg_date\":\"2020-01-23\"," +
                            "\"reg_number\":\"string\"}");
            requestPost.setEntity(stringEntity);
            requestPost.setHeader("Accept", "application/json");
            requestPost.setHeader("Content-type", "application/json");
            response = httpClient.execute(requestPost);
            response.getStatusLine().getStatusCode();
            InputStream inputStream = response.getEntity().getContent();
            StringBuilder stringBuilder = new StringBuilder();
            if (response.getStatusLine().getStatusCode() == 200) {
                Scanner scanner = new Scanner(inputStream);
                while (scanner.hasNext()) {
                    stringBuilder.append(scanner.nextLine());
                }
                System.out.println("response: " + stringBuilder);
            } else {
                System.out.println("StatusCode: " + response.getStatusLine().getStatusCode());
            }
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }

        // добавляем счетчик
        count.incrementAndGet();

        // высчитываем время от начала отсчета
        long currentCountTime = countTime.get() + System.currentTimeMillis() - startTime;
        if (currentCountTime < timeLimit) {
            // Обновляем счетчик времени
            countTime.addAndGet(currentCountTime);
            if (count.get() == requestLimit) {
                //блокируем вызов метода на время, оставшееся до конца цикла времени
                try {
                    Thread.sleep(timeLimit - currentCountTime);
                } catch (
                        InterruptedException e) {
                    throw new RuntimeException(e);
                }
                count.set(0);
                countTime.set(0);
            }
        } else {
            count.set(0);
            countTime.set(0);
        }
    }
}
