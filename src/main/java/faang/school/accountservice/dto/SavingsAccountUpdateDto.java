package faang.school.accountservice.dto;

import faang.school.accountservice.enums.TariffType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SavingsAccountUpdateDto {
    @NotNull(message = "Savings account id cant be null")
    @Min(value = 1, message = "Savings account id should not be less then 1")
    private long savingsAccountId;
    @NotNull(message = "Tariff type cant be null")
    private TariffType tariffType;
}
