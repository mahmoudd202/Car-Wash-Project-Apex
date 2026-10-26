package com.apex.carwash;

import com.apex.carwash.security.ApiKeyFilter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiKeyFilterTest {
    @Test
    void missingOrBlankKeyIsRejected() {
        for (String key : new String[] {null, "", " ", "\t\n"}) {

            assertThrows(IllegalStateException.class, () -> new ApiKeyFilter(key));
        }
    }
}
