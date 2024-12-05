package ro.orange.smartoff.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ro.orange.smartoff.model.ReservationRequest;
import ro.orange.smartoff.model.ReservationResponse;
import ro.orange.smartoff.model.TokenConfig;
import ro.orange.smartoff.model.TokenResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParkingService {

    private static String refresh_token;
    private static String access_token;

    @Autowired
    EmailService emailService;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    TokenConfig tokenConfig;

    @Scheduled(fixedRate = 150000)
    @Retryable(maxAttempts = 2, backoff = @Backoff(2000))
    public void refreshToken(){
        if(refresh_token == null){
            refresh_token = tokenConfig.getInitialToken();
        }
        String url = "https://smartofficesso.orange.ro/auth/realms/ORO/protocol/openid-connect/token";

        TokenResponse response = restTemplate.postForEntity(url, getEntityForRefresh(), TokenResponse.class).getBody();
        refresh_token = response.getRefresh_token();
        access_token = response.getAccess_token();

        System.out.println(Date.from(Instant.now()) + " -- " + response);
    }

    @Scheduled(cron = "2 0 21 * * *")
    @Retryable(maxAttempts = 2, backoff = @Backoff(2000))
    public void makeReservation(){
        refreshToken();
        String url = "https://smartoffice.orange.ro/api/parking/reservation";
        try {
            ReservationResponse[] response = restTemplate.postForEntity(url, getEntityForReservation(), ReservationResponse[].class).getBody();
            System.out.println(Date.from(Instant.now()) + " --  Reservation response: " + response[0]);
        }catch (Exception e){
            System.out.println(e);
            makeReservation();
        }
    }

    //@Scheduled(fixedRate = 300000)
    @Async
    public void checkFreeReservation(){
        refreshToken();
        String url = "https://smartoffice.orange.ro/api/parking/reservation/parkingBasementZoneId/125/fromDate/2024-11-12T00:00:00/toDate/2024-11-12T23:59:59";

        ReservationResponse[] response = restTemplate.exchange(url, HttpMethod.GET, getEntityForCheck(), ReservationResponse[].class).getBody();
        System.out.println("Parked: " + response.length);

        if(response.length != 60){
            System.out.println("Available!");
            String freeNr = getNr(response);
            emailService.sendSimpleMessage("roberta.spurni@gmail.com", "Available parking",
                    "Available parking : " + (60 - response.length) + ", number: " + freeNr);
            System.out.println("Mail sent");
        }
    }

    private String getNr(ReservationResponse[] res){
        int[] totalNr = new int[]{28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95};
        List<Integer> list = Arrays.stream(totalNr).boxed().collect(Collectors.toList());
        String found = "";
        for (Integer nr : list) {
            if (Arrays.stream(res).noneMatch(reservationResponse -> reservationResponse.getAlias().equals(String.valueOf(nr)))) {
                System.out.println("Not found: " + nr);
                found = String.valueOf(nr);
            }
            break;
        }
        return found;
    }

    private HttpEntity<MultiValueMap<String, String>> getEntityForCheck() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Authorization", "Bearer " + access_token);

        return new HttpEntity<>(new LinkedMultiValueMap<>(), headers);
    }

    private HttpEntity<List<ReservationRequest>> getEntityForReservation() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Authorization", "Bearer " + access_token);

        ReservationRequest reservationRequest = ReservationRequest.builder()
                .parkingElementId(25486)
                .start(LocalDateTime.now().withHour(9).withMinute(0).withSecond(0).plusDays(8).toString())
                .end(LocalDateTime.now().withHour(18).withMinute(0).withSecond(0).plusDays(8).toString())
                .isElectricVehicle(false)
                .plateNumber("B188ROA")
                .build();
        List<ReservationRequest> reservations = new ArrayList<>();
        reservations.add(reservationRequest);
        return new HttpEntity<>(reservations, headers);
    }

    private HttpEntity<MultiValueMap<String, String>> getEntityForRefresh() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "refresh_token");
        map.add("refresh_token", refresh_token);
        map.add("scope", "");
        map.add("client_id", "Sense_It");

        return new HttpEntity<>(map, headers);
    }


}
