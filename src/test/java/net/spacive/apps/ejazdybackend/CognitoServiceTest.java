package net.spacive.apps.ejazdybackend;

import net.spacive.apps.ejazdybackend.service.CognitoService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CognitoServiceTest {

    private static final Logger log = LoggerFactory.getLogger(CognitoServiceTest.class.getName());

    @Autowired
    private CognitoService cognitoService;

    @Test
    public void test() {

    }
}
