package com.zky.domain.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderInfoRequestDTO {
    private String userId;
    private List<String> productIds; 
    private String address;
    private String contactName;
    private String contactPhone;
}
