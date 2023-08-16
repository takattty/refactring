package first.example.com;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationCollector;

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


        for (var perf : buildInvoiceJson().get("performances")) {
            var play = playFor(perf);
            var audience = perf.get("audience").asInt();
            int thisAmount = amountFor(audience, play);

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

    /**
     * 1回の演目に対する料金の計算.
     * <p>
     * 処理の中で変更されないため、引数で渡している.<br>
     * 値を変更するのが今回は1つなので、戻り値にしている.
     * @param audience 観客数
     * @param play 演目
     * @return 演目に対する料金
     */
    public static int amountFor(int audience, JsonNode play) {
        int result = 0;
        switch (play.get("type").asText()) {
            case "tragedy" -> {
                result = 40000;
                if (audience > 30) {
                    result += (10000 * (audience - 30));
                }
            }
            case "comedy" -> {
                result = 30000;
                if (audience > 20) {
                    result += (300 * audience);
                }
            }
            default -> throw new Error("unkown type: " + play.get("type"));
        }

        return result;
    }

    /**
     * プレイヤーのIDから、パフォーマー名を取得.
     * <p>
     * @param aPerformance パフォーマンス
     * @return JsonNode パフォーマーID
     * @throws IOException
     */
    public static JsonNode playFor(JsonNode aPerformance) throws IOException {
        return buildPlayJson().get(aPerformance.get("playID").asText());
    }
    public static JsonNode buildInvoiceJson() throws IOException {
        ObjectMapper InvoiceObjectMapper = new ObjectMapper();
        Path invoicePath = Paths.get("/Users/takatty/software/java/refactring/src/main/java/first/example/com/invoice.json");
        return InvoiceObjectMapper.readTree(invoicePath.toFile()).get(0);
    }

    public static JsonNode buildPlayJson() throws IOException {
        ObjectMapper playObjectMapper = new ObjectMapper();
        Path playPath = Paths.get("/Users/takatty/software/java/refactring/src/main/java/first/example/com/plays.json");
        return playObjectMapper.readTree(playPath.toFile());
    }
}
