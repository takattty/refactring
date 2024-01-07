package first.example.com;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Locale;

public class Main {

    public static void main(String[] args) throws IOException {
        statement(buildInvoiceJson());
    }

    public static void statement(JsonNode invoice) throws IOException {
        int totalAmount = 0;
        int volumeCredits = 0;
        String result = "=====Statement for BigCo=====\n";

        // ロケールとオプションを指定してフォーマットする
        Locale locale = new Locale("en", "US"); // 日本のロケールを使用
        NumberFormat format = NumberFormat.getNumberInstance(locale);
        format.setMaximumFractionDigits(2); // 小数点以下の最大桁数を指定

        for (var perf : invoice.get("performances")) {
            volumeCredits += volumeCreditsFor(perf);
            // 注文の内訳を出力
            result += " " + playFor(perf).get("name").asText() + ": " + format.format(amountFor(perf) / 100) + " " + perf.get("audience").asInt() + "seats \n";
            totalAmount += amountFor(perf);
        }
        result += "Amount owed is " + format.format(totalAmount / 100) + "\n";
        result += "=====You earned " + volumeCredits + " credits=====\n";
        System.out.println(result);
    }

    public static int volumeCreditsFor(JsonNode aPerformance) throws IOException {
        int result = 0;
        // ボリューム特典のポイントを加算
        result += Math.max(aPerformance.get("audience").asInt() - 30, 0);
        // 喜劇の時は10人につき、更にポイントを加算
        if ("comedy".equals(playFor(aPerformance).get("type").asText())) result += (aPerformance.get("audience").asInt() / 5);

        return result;
    }

    /**
     * 1回の演目に対する料金の計算.
     * <p>
     * 処理の中で変更されないため、引数で渡している.<br>
     * 値を変更するのが今回は1つなので、戻り値にしている.
     *
     * @param perf 観客
     * @return 演目に対する料金
     */
    public static int amountFor(JsonNode perf) throws IOException {
        int result = 0;
        switch (playFor(perf).get("type").asText()) {
            case "tragedy" -> {
                result = 40000;
                if (perf.get("audience").asInt() > 30) {
                    result += (10000 * (perf.get("audience").asInt() - 30));
                }
            }
            case "comedy" -> {
                result = 30000;
                if (perf.get("audience").asInt() > 20) {
                    result += (300 * perf.get("audience").asInt());
                }
            }
            default -> throw new Error("unkown type: " + playFor(perf).get("type"));
        }

        return result;
    }

    /**
     * プレイヤーのIDから、パフォーマー名を取得.
     * <p>
     *
     * @param perf パフォーマンス
     * @return JsonNode パフォーマーID
     */
    public static JsonNode playFor(JsonNode perf) throws IOException {
        return buildPlayJson().get(perf.get("playID").asText());
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
