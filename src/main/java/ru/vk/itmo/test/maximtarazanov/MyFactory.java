package ru.vk.itmo.test.maximtarazanov;

import ru.vk.itmo.Service;
import ru.vk.itmo.ServiceConfig;
import ru.vk.itmo.test.ServiceFactory;

@ServiceFactory(stage = 1)
public class MyFactory implements ServiceFactory.Factory {

    @Override
    public Service create(ServiceConfig config) {
        return new MyService(config);
    }
}
