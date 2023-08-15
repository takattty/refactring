package first.example.com;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        int totalAmount = 0;
        int volumeCredits = 0;
        String result = "Statement for BigCo\n";

        // ロケールとオプションを指定してフォーマットする
        Locale locale = new Locale("en", "US"); // 日本のロケールを使用
        NumberFormat format = NumberFormat.getNumberInstance(locale);
        format.setMaximumFractionDigits(2); // 小数点以下の最大桁数を指定

        ObjectMapper InvoiceObjectMapper = new ObjectMapper();
        Path invoicePath = Paths.get("/Users/takatty/software/java/refactring/src/main/java/first/example/com/invoice.json");
        var invoiceJson = InvoiceObjectMapper.readTree(invoicePath.toFile()).get(0);

        ObjectMapper playObjectMapper = new ObjectMapper();
        Path playPath = Paths.get("/Users/takatty/software/java/refactring/src/main/java/first/example/com/plays.json");
        var playJson = playObjectMapper.readTree(playPath.toFile());


        for (var perf : invoiceJson.get("performances")) {
            var play = playJson.get(perf.get("playID").asText());
            var audience = perf.get("audience").asInt();
            int thisAmount = 0;

            switch (play.get("type").asText()) {
                case "tragedy" -> {
                    thisAmount = 40000;
                    if (audience > 30) {
                        thisAmount += (10000 * (audience - 30));
                    }
                }
                case "comedy" -> {
                    thisAmount = 30000;
                    if (audience > 20) {
                        thisAmount += (300 * audience);
                    }
                }
                default -> throw new Error("unkown type: " + play.get("type"));
            }

            // ボリューム特典のポイントを加算
            volumeCredits += Math.max(audience - 30, 0);
            // 喜劇の時は10人につき、更にポイントを加算
            if ("comedy".equals(play.get("type").asText())) volumeCredits += (audience / 5);
            // 注文の内訳を出力
            result += " " + play.get("name").asText() + ": " + format.format(thisAmount / 100) + " " + audience + "seats \n";
            totalAmount += thisAmount;
        }
        result += "Amount owed is " + format.format(totalAmount/100) + "\n";
        result += "You earned " + volumeCredits + " credits\n";
        System.out.println(result);
    }
}