package com.davendra.calistrack_backend.user.mapper;

import com.davendra.calistrack_backend.catalog.entity.Node;
import com.davendra.calistrack_backend.user.dto.GoalNodeSummary;
import com.davendra.calistrack_backend.user.dto.MeResponse;
import com.davendra.calistrack_backend.user.entity.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.time.LocalDate;
import java.time.Period;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserProfileMapper {

	@Mapping(source = "currentGoalNode", target = "goal")
	@Mapping(target = "age", expression = "java(resolveAge(user))")
	MeResponse toMeResponse(AppUser user);

	GoalNodeSummary toGoalSummary(Node node);

	default Integer resolveAge(AppUser user) {
		if (user.getDateOfBirth() != null) {
			return Period.between(user.getDateOfBirth(), LocalDate.now()).getYears();
		}
		return user.getAge();
	}
}
