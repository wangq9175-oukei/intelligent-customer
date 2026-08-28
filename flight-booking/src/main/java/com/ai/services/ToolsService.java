package com.ai.services;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Classname ToolsService
 * @Description TODO
 * @Date 2026/8/14 17:08
 * @Created Alex
 */
@Service
public class ToolsService {

    @Autowired
    private FlightBookingService flightBookingService;

    @Tool(description = "退票/取消预定，调用之前先查询航班信息")
    public String cancelBooking(@ToolParam(description = "航班号，可以是纯数字") String bookingNumber,
                                @ToolParam(description = "姓名") String name) {
        System.out.println("取消预定" + bookingNumber + " " + name);
        flightBookingService.cancelBooking(bookingNumber, name);
        return "退票成功";
    }

    @Tool(description = "查询航班详情")
    public BookingTools.BookingDetails getBookingDetails(@ToolParam(description = "航班号，可以是纯数字") String bookingNumber,
                                                         @ToolParam(description = "姓名") String name) {

        return flightBookingService.getBookingDetails(bookingNumber, name);
    }
}
