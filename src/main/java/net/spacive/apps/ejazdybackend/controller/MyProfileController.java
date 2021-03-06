package net.spacive.apps.ejazdybackend.controller;

import net.spacive.apps.ejazdybackend.model.CognitoUser;
import net.spacive.apps.ejazdybackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for profile resource.
 *
 * @author  Juraj Haluska
 */
@RestController
@CrossOrigin
@RequestMapping("/profile")
public class MyProfileController {

    /**
     * Instance of UserService.
     */
    private final UserService userService;

    /**
     * Constructor
     *
     * @param userService injected param
     */
    @Autowired
    public MyProfileController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get profile of calling user.
     *
     * @param auth security object which contains principal.
     * @return instance of user.
     */
    @GetMapping
    public CognitoUser getMyProfile(Authentication auth) {
        CognitoUser fromToken = (CognitoUser) auth.getPrincipal();

        // fromToken might not contain all attributes
        // so we need to fetch them from user service
        return userService.getUser(fromToken.getId());
    }
}
