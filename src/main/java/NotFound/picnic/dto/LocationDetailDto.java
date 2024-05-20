package NotFound.picnic.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDetailDto {
    private String name;
    private String address;
    private String detail;
    private Double latitude;
    private Double longitude;
    private String division;
    private String phone;
    private List<String> imageUrls;

    private Accommodation accommodation;
    private Culture culture;
    private Festival festival;
    private Leisure leisure;
    private Restaurant restaurant;
    private Shopping shopping;
    private Tour tour;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Accommodation {
        private String checkIn;
        private String checkOut;
        private String cook;
        private String detail;
        private String parking;
        private String reservation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Culture {
        private String babycar;
        private String detail;
        private String discount;
        private String fee;
        private String offDate;
        private String parking;
        private String pet;
        private String time;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Festival {
        private String detail;
        private String startDate;
        private String endDate;
        private String fee;
        private String time;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Leisure {
        private String babycar;
        private String detail;
        private String fee;
        private String openDate;
        private String offDate;
        private String parking;
        private String pet;
        private String time;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Restaurant {
        private String dayOff;
        private String mainMenu;
        private String menu;
        private String packaging;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Shopping {
        private String babycar;
        private String offDate;
        private String parking;
        private String pet;
        private String time;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Tour {
        private String detail;
        private String offDate;
        private String parking;
        private String pet;
        private String time;
    }
}
