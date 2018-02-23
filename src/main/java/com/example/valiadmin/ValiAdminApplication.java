package com.example.valiadmin;

import com.entity.Role;
import com.entity.User;
import com.repo.RoleRepo;
import com.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@SpringBootApplication
@EntityScan("com.entity")
@EnableJpaRepositories("com.repo")
public class ValiAdminApplication {
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private RoleRepo roleRepo;
	@Autowired
	private MyuserService myuserService;

	@Autowired
	private ShaPasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(ValiAdminApplication.class, args);
	}




	@Bean
	public CommandLineRunner commandLineRunner() {
		return(x) -> {
			Role role = new Role();
			role.setRoleName("ROLE_ADMIN");
			role.setAuditDate(new Date());
			roleRepo.save(role);

			User user = new User();
			user.setUsername("admin");
			String password = passwordEncoder.encodePassword("123",null);
			user.setPassword(password);
			user.setEnabled(true);
			user.setAuditDate(new Date());
			userRepo.save(user);

			Set<User> userSet = new HashSet<>();
			userSet.add(user);

			role.setUsers(userSet);
			roleRepo.save(role);

			Set<Role> roleSet = new HashSet<>();
			roleSet.add(role);

			user.setRoles(roleSet);
			userRepo.save(user);
			System.out.println(user.getPassword());
		};
	}

	@Controller
	public class Mycontroller {

		@GetMapping("/index")
		public String showIndexPage() {
			return "index";
		}

		@GetMapping("/login")
		public String showLoginPage(Model model,String logout,String error) {
			if(logout!=null) {
				model.addAttribute("login-alert","You are Logout");
			}
			if(error!=null) {
				model.addAttribute("login-alert","Error");
			}
			User user = userRepo.findOne("admin");
			return "page-login";
		}
	}


	@Service
	public class MyuserService implements UserDetailsService {
		@Override
		public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
			User user = userRepo.findOne(s);
			StringJoiner stringJoiner = new StringJoiner(",");
			for(Role role : user.getRoles()) {
				stringJoiner.add(role.getRoleName());
			}
			return new org.springframework.security.core.userdetails.User(
					user.getUsername(),
					user.getPassword(),
					AuthorityUtils.commaSeparatedStringToAuthorityList(stringJoiner.toString()));

		}
	}


	@Bean
	public ShaPasswordEncoder passwordEncoder() {
		ShaPasswordEncoder shaPasswordEncoder = new ShaPasswordEncoder(256);
		shaPasswordEncoder.setEncodeHashAsBase64(true);
		return shaPasswordEncoder;
	}

	@EnableWebSecurity
	public class Mysecurity extends WebSecurityConfigurerAdapter {


		@Override
		protected void configure(AuthenticationManagerBuilder auth) throws Exception {
			auth
					.userDetailsService(myuserService)
					.passwordEncoder(passwordEncoder());
		}



		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
					.authorizeRequests()
					.antMatchers("/css/**,/font-awesome/**,/js/**").permitAll()
					.antMatchers("/index/**").hasAnyRole("ADMIN,USER")
					.and()
					.formLogin().loginPage("/login").defaultSuccessUrl("/index",true)
					.and()
					.csrf().disable();
		}
	}
}
