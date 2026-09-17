package com.gorest.data;

import com.gorest.constants.FrameworkConstants;
import com.gorest.pojo.User;
import com.gorest.utils.TestDataUtility;
import org.testng.annotations.DataProvider;

/**
 * Test data feeds for the user suite.
 */
public class UserDataProvider {

    /** Valid users covering both statuses and both genders. */
    @DataProvider(name = "validUsers")
    public Object[][] validUsers() {
        return new Object[][]{
                {"active male user",
                        new User(TestDataUtility.randomName(), TestDataUtility.uniqueEmail(),
                                FrameworkConstants.GENDER_MALE, FrameworkConstants.STATUS_ACTIVE)},
                {"inactive female user",
                        new User(TestDataUtility.randomName(), TestDataUtility.uniqueEmail(),
                                FrameworkConstants.GENDER_FEMALE, FrameworkConstants.STATUS_INACTIVE)}
        };
    }

    /**
     * Invalid users the API must reject with 422.
     * Columns: scenario, payload, field expected in the error response.
     */
    @DataProvider(name = "invalidUsers")
    public Object[][] invalidUsers() {
        return new Object[][]{
                {"blank name",
                        new User("", TestDataUtility.uniqueEmail(),
                                FrameworkConstants.GENDER_MALE, FrameworkConstants.STATUS_ACTIVE),
                        "name"},
                {"blank email",
                        new User(TestDataUtility.randomName(), "",
                                FrameworkConstants.GENDER_MALE, FrameworkConstants.STATUS_ACTIVE),
                        "email"},
                {"malformed email",
                        new User(TestDataUtility.randomName(), "not-an-email",
                                FrameworkConstants.GENDER_MALE, FrameworkConstants.STATUS_ACTIVE),
                        "email"},
                {"unsupported gender",
                        new User(TestDataUtility.randomName(), TestDataUtility.uniqueEmail(),
                                "unknown", FrameworkConstants.STATUS_ACTIVE),
                        "gender"},
                {"blank status",
                        new User(TestDataUtility.randomName(), TestDataUtility.uniqueEmail(),
                                FrameworkConstants.GENDER_MALE, ""),
                        "status"}
        };
    }
}
