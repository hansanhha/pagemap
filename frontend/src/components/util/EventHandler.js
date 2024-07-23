import EventEmitter from "events";

const eventEmitter = new EventEmitter();

export const publishEvent = (eventName, data) => {
    eventEmitter.emit(eventName, data);
}

export const subscribeEvent = (eventName, callback) => {
    eventEmitter.on(eventName, callback);
}

export const unsubscribeEvent = (eventName, callback) => {
    eventEmitter.off(eventName, callback);
}
