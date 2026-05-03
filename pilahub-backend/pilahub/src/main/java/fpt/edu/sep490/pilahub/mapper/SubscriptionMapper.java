package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.SubscriptionDto;
import fpt.edu.sep490.pilahub.pojo.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {TraineeMapper.class, PackageMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SubscriptionMapper {

    SubscriptionDto toDto(Subscription subscription);
}
