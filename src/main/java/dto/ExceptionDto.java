package dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ExceptionDto {
    String ExceptionCode;
    String ExceptionDescription;
    String ActionRequired;
}
