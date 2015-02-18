/*******************************************************************************

 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  ________ __________.______________
 *  \_____  \\______   \   \__    ___/
 *   /  / \  \|    |  _/   | |    |  ______
 *  /   \_/.  \    |   \   | |    | /_____/
 *  \_____\ \_/______  /___| |____|
 *         \__>      \/
 *  ___________.__                  ____.                        _____  .__                                             .__
 *  \__    ___/|  |__   ____       |    |____ ___  _______      /     \ |__| ___________  ____  ______ ______________  _|__| ____  ____
 *    |    |   |  |  \_/ __ \      |    \__  \\  \/ /\__  \    /  \ /  \|  |/ ___\_  __ \/  _ \/  ___// __ \_  __ \  \/ /  |/ ___\/ __ \
 *    |    |   |   Y  \  ___/  /\__|    |/ __ \\   /  / __ \_ /    Y    \  \  \___|  | \(  <_> )___ \\  ___/|  | \/\   /|  \  \__\  ___/
 *    |____|   |___|  /\___  > \________(____  /\_/  (____  / \____|__  /__|\___  >__|   \____/____  >\___  >__|    \_/ |__|\___  >___  >
 *                  \/     \/                \/           \/          \/        \/                 \/     \/                    \/    \/
 *  .____    ._____.
 *  |    |   |__\_ |__
 *  |    |   |  || __ \
 *  |    |___|  || \_\ \
 *  |_______ \__||___  /
 *          \/       \/
 *       ____. _________________    _______         __      __      ___.     _________              __           __      _____________________ ____________________
 *      |    |/   _____/\_____  \   \      \       /  \    /  \ ____\_ |__  /   _____/ ____   ____ |  | __ _____/  |_    \______   \_   _____//   _____/\__    ___/
 *      |    |\_____  \  /   |   \  /   |   \      \   \/\/   // __ \| __ \ \_____  \ /  _ \_/ ___\|  |/ // __ \   __\    |       _/|    __)_ \_____  \   |    |
 *  /\__|    |/        \/    |    \/    |    \      \        /\  ___/| \_\ \/        (  <_> )  \___|    <\  ___/|  |      |    |   \|        \/        \  |    |
 *  \________/_______  /\_______  /\____|__  / /\    \__/\  /  \___  >___  /_______  /\____/ \___  >__|_ \\___  >__| /\   |____|_  /_______  /_______  /  |____|
 *                   \/         \/         \/  )/         \/       \/    \/        \/            \/     \/    \/     )/          \/        \/        \/
 *  __________           __  .__              __      __      ___.
 *  \______   \ ____   _/  |_|  |__   ____   /  \    /  \ ____\_ |__
 *  |    |  _// __ \  \   __\  |  \_/ __ \  \   \/\/   // __ \| __ \
 *   |    |   \  ___/   |  | |   Y  \  ___/   \        /\  ___/| \_\ \
 *   |______  /\___  >  |__| |___|  /\___  >   \__/\  /  \___  >___  /
 *          \/     \/             \/     \/         \/       \/    \/
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 *  http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
 *  http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
 *  http://rick-hightower.blogspot.com/2015/01/quick-start-qbit-programming.html
 *  http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
 *  http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html

 ******************************************************************************/

package io.advantageous.qbit.server;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.websocket.WebSocketMessage;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueueListener;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.ProtocolParser;
import io.advantageous.qbit.system.QBitSystemManager;
import org.boon.core.Sys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.boon.Boon.puts;

/**
 * Created by rhightower on 10/22/14.
 *
 * @author rhightower
 */
public class ServiceServerImpl implements ServiceServer {


    protected final int batchSize;
    private final Logger logger = LoggerFactory.getLogger(ServiceServerImpl.class);
    private final boolean debug = false || GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final QBitSystemManager systemManager;
    protected WebSocketServiceServerHandler webSocketHandler;
    protected HttpRequestServiceServerHandler httpRequestServerHandler;
    protected int timeoutInSeconds = 30;
    protected ProtocolEncoder encoder;
    protected HttpServer httpServer;
    protected ServiceBundle serviceBundle;
    protected JsonMapper jsonMapper;
    protected ProtocolParser parser;
    protected Object context = Sys.contextToHold();


    private AtomicBoolean stop = new AtomicBoolean();


    public ServiceServerImpl(final HttpServer httpServer, final ProtocolEncoder encoder, final ProtocolParser parser, final ServiceBundle serviceBundle, final JsonMapper jsonMapper, final int timeOutInSeconds, final int numberOfOutstandingRequests, final int batchSize, final int flushInterval, final QBitSystemManager systemManager) {

        this.systemManager = systemManager;
        this.encoder = encoder;
        this.parser = parser;
        this.httpServer = httpServer;
        this.serviceBundle = serviceBundle;
        this.jsonMapper = jsonMapper;
        this.timeoutInSeconds = timeOutInSeconds;
        this.batchSize = batchSize;

        webSocketHandler = new WebSocketServiceServerHandler(batchSize, serviceBundle, encoder, parser);
        httpRequestServerHandler = new HttpRequestServiceServerHandler(this.timeoutInSeconds, this.encoder, this.parser, serviceBundle, jsonMapper, numberOfOutstandingRequests, flushInterval);
    }


    @Override
    public void start() {


        stop.set(false);

        httpServer.setHttpRequestConsumer(httpRequestServerHandler::handleRestCall);
        httpServer.setWebSocketMessageConsumer(webSocketHandler::handleWebSocketCall);
        httpServer.setWebSocketCloseConsumer(webSocketHandler::handleWebSocketClose);
        httpServer.setHttpRequestsIdleConsumer(httpRequestServerHandler::httpRequestQueueIdle);

        httpServer.setWebSocketIdleConsume(webSocketHandler::webSocketQueueIdle);


        serviceBundle.start();
        startResponseQueueListener();
        httpServer.start();


    }

    public void stop() {

        try {
            serviceBundle.stop();
        } catch ( Exception ex ) {
            if ( debug ) logger.debug("Unable to cleanly shutdown bundle", ex);
        }

        try {
            httpServer.stop();
        } catch ( Exception ex ) {
            if ( debug ) logger.debug("Unable to cleanly shutdown httpServer", ex);
        }


        if ( systemManager != null ) systemManager.serviceShutDown();

    }


    /**
     * Sets up the response queue listener so we can sendText responses
     * to HTTP / WebSocket end points.
     */
    private void startResponseQueueListener() {
        serviceBundle.startReturnHandlerProcessor(createResponseQueueListener());
    }

    /**
     * Creates the queue listener for method call responses from the client bundle.
     *
     * @return the response queue listener to handle the responses to method calls.
     */
    private ReceiveQueueListener<Response<Object>> createResponseQueueListener() {
        return new ReceiveQueueListener<Response<Object>>() {


            List<Response<Object>> responseBatch = new ArrayList<>();

            @Override
            public void receive(final Response<Object> response) {

                if ( debug ) {
                    puts("createResponseQueueListener() Received a response", response);
                }

                responseBatch.add(response);

                if ( responseBatch.size() >= batchSize ) {
                    handleResponseFromServiceBundle(new ArrayList<>(responseBatch));
                    responseBatch.clear();
                }

            }


            @Override
            public void limit() {


                handleResponseFromServiceBundle(new ArrayList<>(responseBatch));
                responseBatch.clear();

                httpRequestServerHandler.checkTimeoutsForRequests();
                webSocketHandler.checkResponseBatchSend();
            }

            @Override
            public void empty() {
                handleResponseFromServiceBundle(new ArrayList<>(responseBatch));
                responseBatch.clear();

                httpRequestServerHandler.checkTimeoutsForRequests();
                webSocketHandler.checkResponseBatchSend();

            }

            @Override
            public void idle() {

                handleResponseFromServiceBundle(new ArrayList<>(responseBatch));
                responseBatch.clear();


                httpRequestServerHandler.checkTimeoutsForRequests();
                webSocketHandler.checkResponseBatchSend();
            }
        };
    }


    /**
     * Handle a response from the server.
     *
     * @param responses responses
     */
    private void handleResponseFromServiceBundle(final List<Response<Object>> responses) {


        for ( Response<Object> response : responses ) {

            final Request<Object> request = response.request();

            if ( request instanceof MethodCall ) {


                final MethodCall<Object> methodCall = ( ( MethodCall<Object> ) request );
                final Request<Object> originatingRequest = methodCall.originatingRequest();

                handleResponseFromServiceBundle(response, originatingRequest);

            }
        }

    }

    private void handleResponseFromServiceBundle(final Response<Object> response, final Request<Object> originatingRequest) {

        /* TODO Since websockets can be for many requests, we need a counter of some sort. */

        if ( originatingRequest instanceof HttpRequest ) {

            if ( originatingRequest.isHandled() ) {
                return; // the operation timed out
            }
            originatingRequest.handled(); //Let others know that it is handled.




            httpRequestServerHandler.handleResponseFromServiceToHttpResponse(response, ( HttpRequest ) originatingRequest);
        } else if ( originatingRequest instanceof WebSocketMessage ) {
            originatingRequest.handled(); //Let others know that it is handled.

            webSocketHandler.handleResponseFromServiceBundleToWebSocketSender(response, ( WebSocketMessage ) originatingRequest);
        } else {

            throw new IllegalStateException("Unknown response " + response);
        }
    }

    @Override
    public ServiceServer flush() {
        this.serviceBundle.flush();
        return this;
    }


    @Override
    public ServiceServer initServices(Iterable services) {


        for ( Object service : services ) {
            if ( debug ) logger.debug("registering service: " + service.getClass().getName());
            serviceBundle.addService(service);
            httpRequestServerHandler.addRestSupportFor(service.getClass(), serviceBundle.address());
        }

        return this;

    }


    @Override
    public ServiceServer initServices(Object... services) {


        for ( Object service : services ) {
            if ( debug ) logger.debug("registering service: " + service.getClass().getName());
            serviceBundle.addService(service);
            httpRequestServerHandler.addRestSupportFor(service.getClass(), serviceBundle.address());
        }

        return this;

    }


}
