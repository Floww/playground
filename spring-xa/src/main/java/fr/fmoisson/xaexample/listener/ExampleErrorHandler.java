package fr.fmoisson.xaexample.listener;

import org.springframework.util.ErrorHandler;

/**
 * Created by fmoisson on 15/03/15.
 */
public class ExampleErrorHandler implements ErrorHandler {
    @Override
    public void handleError(Throwable t) {
        System.out.println("Message Error Handler : "+t.getMessage());
    }
}
