package org.example.roomrelish.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.roomrelish.models.Customer;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NavbarResponse {
    private boolean success;
    private CustomerProfile info;
}