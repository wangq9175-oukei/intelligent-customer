package com.ai.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.ai.data.BookingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.function.Function;

@Service
public class BookingTools {

    private static final Logger logger = LoggerFactory.getLogger(BookingTools.class);

    @Autowired
    private FlightBookingService flightBookingService;


    public record ChangeBookingDatesRequest(String bookingNumber, String name, String date, String from, String to) {
    }


    public record BookingDetailsRequest(String bookingNumber, String name) {
    }


    @JsonInclude(Include.NON_NULL)
    public record BookingDetails(String bookingNumber, String name, LocalDate date, BookingStatus bookingStatus,
            String from, String to, String bookingClass) {
    }


    @Tool(description = "获取机票预定详细信息")
    BookingDetails getBookingDetails( @ToolParam(description = "预定号")
                                      String bookingNumber,
                                      @ToolParam(description = "姓名")
                                      String name) {
        try {
            return flightBookingService.getBookingDetails(bookingNumber,name);
        }
        catch (Exception e) {
            logger.warn("Booking details: {}", NestedExceptionUtils.getMostSpecificCause(e).getMessage());
            return new BookingDetails(bookingNumber, name, null, null, null, null, null);
        }
    }


//    @Tool(description = "修改机票预定日期")
//    String changeBooking(ChangeBookingDatesRequest bookingDetailsRequest) {
//        flightBookingService.changeBooking(bookingDetailsRequest.bookingNumber(), bookingDetailsRequest.name(),
//                bookingDetailsRequest.date(), bookingDetailsRequest.from(),bookingDetailsRequest.to());
//        return "";
//    }

    @Tool(description = "取消机票预定")
    String cancelBooking(@ToolParam(description = "预定号")
                         String bookingNumber,
                         @ToolParam(description = "姓名")
                         String name) {
        flightBookingService.cancelBooking(bookingNumber, name );

        return "";
    }

    @Tool(description = "修改机票预定的出发地或目的地。仅用于改签，不会取消原预订，也不需要重新预订。")
    String changeBooking(@ToolParam(description = "预定号") String bookingNumber,
                         @ToolParam(description = "姓名") String name,
                         @ToolParam(description = "新的出发地") String from,
                         @ToolParam(description = "新的目的地") String to) {
        if (from == null || to == null) {
            return "请指定新的出发地或目的地";
        }

        BookingDetails booking = flightBookingService.getBookingDetails(bookingNumber, name);

        if (from.equals(booking.from()) && to.equals(booking.to())) {
            return "请指定新的出发地或目的地";
        }
        if (booking.date().isBefore(LocalDate.now().plusDays(2))) {
            return "改签失败，改签日期不能早于当前日期2天。";
        }
        if (booking.bookingStatus() != BookingStatus.CONFIRMED) {
            return "改签失败，请等待航班开始后再进行改签。";
        }
        if (!flightBookingService.checkAirportCode( from)) {
            return "出发地 " + from + " 不存在";
        }
        if (!flightBookingService.checkAirportCode( to)) {
            return "目的地 " + to + " 不存在";
        }

        flightBookingService.changeBooking(bookingNumber, name, booking.date().toString(), from, to);
        return "预订 " + bookingNumber + " 已改签为 " + from + "→" + to + "，原预订仍然有效。";
    }

    public record CancelBookingRequest(@ToolParam(description = "预定号")
                                       String bookingNumber,
                                       @ToolParam(description = "姓名")
                                       String name) {
    }



    /*@Bean
    @Description("获取机票预定详细信息")
    public Function<BookingDetailsRequest, BookingDetails> getBookingDetails() {
        return request -> {
            try {
                return flightBookingService.getBookingDetails(request.bookingNumber(), request.name());
            }
            catch (Exception e) {
                logger.warn("Booking details: {}", NestedExceptionUtils.getMostSpecificCause(e).getMessage());
                return new BookingDetails(request.bookingNumber(), request.name(), null, null, null, null, null);
            }
        };
    }*/

    @Bean
    @Description("修改机票预定日期")
    public Function<ChangeBookingDatesRequest, String> changeBooking() {
        return request -> {
            flightBookingService.changeBooking(request.bookingNumber(), request.name(), request.date(), request.from(),
                    request.to());
            return "";
        };
    }

    @Bean
    @Description("修改目的地或出发地")
    public Function<ChangeBookingDatesRequest, String> changeBookingFromTo() {
        return request -> {
            flightBookingService.changeBooking(request.bookingNumber(), request.name(), request.date(), request.from(),
                    request.to());
            return "";
        };
    }

    /*@Bean
    @Description("取消机票预定")
    public Function<CancelBookingRequest, String> cancelBooking() {
        return request -> {
            flightBookingService.cancelBooking(request.bookingNumber(), request.name());
            return "";
        };
    }*/

}
