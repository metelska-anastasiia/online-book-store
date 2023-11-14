package mate.academy.dto.order;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ShippingAddressRequestDto {
    private String shippingAddress;
}
