package am.matveev.TelegramBot.service;

import am.matveev.TelegramBot.config.BotConfig;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherService {
    private final BotConfig config;

    public WeatherService(BotConfig config){
        this.config = config;
    }


    public String getWeatherInYerevan() {
        try {
            String apiKey = config.getWeatherApiKey();
            String city = "Yerevan";
            String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey;

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                double temperature = jsonObject.getJSONObject("main").getDouble("temp") - 273.15;
                String description = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
                return String.format("Погода в Ереване: %.1f°C, %s", temperature, description);
            } else {
                return "Ошибка при получении данных о погоде";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Ошибка при получении данных о погоде";
        }
    }
}
