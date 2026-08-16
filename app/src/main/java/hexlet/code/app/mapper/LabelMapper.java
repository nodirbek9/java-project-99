package hexlet.code.app.mapper;

import hexlet.code.app.dto.LabelCreateDTO;
import hexlet.code.app.dto.LabelDTO;
import hexlet.code.app.dto.LabelUpdateDTO;
import hexlet.code.app.entity.Label;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        uses = {JsonNullableMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface LabelMapper {

    Label map(LabelCreateDTO dto);

    LabelDTO map(Label model);

    void update(LabelUpdateDTO dto, @MappingTarget Label model);
}
