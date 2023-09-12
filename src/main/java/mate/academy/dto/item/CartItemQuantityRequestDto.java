package mate.academy.dto.item;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CartItemQuantityRequestDto {
    @Min(1)
    @Max(100)
    private int quantity;
}
