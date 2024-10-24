package dev.realtards.wzsnacknbites.dtos.account;

import dev.realtards.wzsnacknbites.models.Account;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "Privilege update request")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PrivilegeUpdateDto {

	@Schema(description = "New privilege level to set",	example = "user")
	@NotBlank(message = "Privilege is required")
	@Pattern(regexp = "^(admin|user)$", message = "Privilege must be either 'admin' or 'user'")
	private String privilege;

	public Account.Privilege getPrivilege() {
		return Account.Privilege.fromString(privilege);
	}

	public PrivilegeUpdateDto(Account.Privilege privilege) {
		this.privilege = privilege.getPrivilege();
	}
}
