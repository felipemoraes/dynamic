package br.ufmg.dcc.latin.grpc.learner;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 0.14.0)",
    comments = "Source: learner.proto")
public class LearnerGrpc {

  private LearnerGrpc() {}

  public static final String SERVICE_NAME = "learner.Learner";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<br.ufmg.dcc.latin.grpc.learner.LoadRequest,
      br.ufmg.dcc.latin.grpc.learner.LoadReply> METHOD_LOAD =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "learner.Learner", "load"),
          io.grpc.protobuf.ProtoUtils.marshaller(br.ufmg.dcc.latin.grpc.learner.LoadRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(br.ufmg.dcc.latin.grpc.learner.LoadReply.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<br.ufmg.dcc.latin.grpc.learner.SimilarityRequest,
      br.ufmg.dcc.latin.grpc.learner.SimilarityResponse> METHOD_GET_SIMILARITIES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "learner.Learner", "getSimilarities"),
          io.grpc.protobuf.ProtoUtils.marshaller(br.ufmg.dcc.latin.grpc.learner.SimilarityRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(br.ufmg.dcc.latin.grpc.learner.SimilarityResponse.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static LearnerStub newStub(io.grpc.Channel channel) {
    return new LearnerStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static LearnerBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new LearnerBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static LearnerFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new LearnerFutureStub(channel);
  }

  /**
   */
  public static interface Learner {

    /**
     */
    public void load(br.ufmg.dcc.latin.grpc.learner.LoadRequest request,
        io.grpc.stub.StreamObserver<br.ufmg.dcc.latin.grpc.learner.LoadReply> responseObserver);

    /**
     */
    public void getSimilarities(br.ufmg.dcc.latin.grpc.learner.SimilarityRequest request,
        io.grpc.stub.StreamObserver<br.ufmg.dcc.latin.grpc.learner.SimilarityResponse> responseObserver);
  }

  @io.grpc.ExperimentalApi
  public static abstract class AbstractLearner implements Learner, io.grpc.BindableService {

    @java.lang.Override
    public void load(br.ufmg.dcc.latin.grpc.learner.LoadRequest request,
        io.grpc.stub.StreamObserver<br.ufmg.dcc.latin.grpc.learner.LoadReply> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_LOAD, responseObserver);
    }

    @java.lang.Override
    public void getSimilarities(br.ufmg.dcc.latin.grpc.learner.SimilarityRequest request,
        io.grpc.stub.StreamObserver<br.ufmg.dcc.latin.grpc.learner.SimilarityResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_SIMILARITIES, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return LearnerGrpc.bindService(this);
    }
  }

  /**
   */
  public static interface LearnerBlockingClient {

    /**
     */
    public br.ufmg.dcc.latin.grpc.learner.LoadReply load(br.ufmg.dcc.latin.grpc.learner.LoadRequest request);

    /**
     */
    public br.ufmg.dcc.latin.grpc.learner.SimilarityResponse getSimilarities(br.ufmg.dcc.latin.grpc.learner.SimilarityRequest request);
  }

  /**
   */
  public static interface LearnerFutureClient {

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<br.ufmg.dcc.latin.grpc.learner.LoadReply> load(
        br.ufmg.dcc.latin.grpc.learner.LoadRequest request);

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<br.ufmg.dcc.latin.grpc.learner.SimilarityResponse> getSimilarities(
        br.ufmg.dcc.latin.grpc.learner.SimilarityRequest request);
  }

  public static class LearnerStub extends io.grpc.stub.AbstractStub<LearnerStub>
      implements Learner {
    private LearnerStub(io.grpc.Channel channel) {
      super(channel);
    }

    private LearnerStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LearnerStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new LearnerStub(channel, callOptions);
    }

    @java.lang.Override
    public void load(br.ufmg.dcc.latin.grpc.learner.LoadRequest request,
        io.grpc.stub.StreamObserver<br.ufmg.dcc.latin.grpc.learner.LoadReply> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LOAD, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getSimilarities(br.ufmg.dcc.latin.grpc.learner.SimilarityRequest request,
        io.grpc.stub.StreamObserver<br.ufmg.dcc.latin.grpc.learner.SimilarityResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_SIMILARITIES, getCallOptions()), request, responseObserver);
    }
  }

  public static class LearnerBlockingStub extends io.grpc.stub.AbstractStub<LearnerBlockingStub>
      implements LearnerBlockingClient {
    private LearnerBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private LearnerBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LearnerBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new LearnerBlockingStub(channel, callOptions);
    }

    @java.lang.Override
    public br.ufmg.dcc.latin.grpc.learner.LoadReply load(br.ufmg.dcc.latin.grpc.learner.LoadRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LOAD, getCallOptions(), request);
    }

    @java.lang.Override
    public br.ufmg.dcc.latin.grpc.learner.SimilarityResponse getSimilarities(br.ufmg.dcc.latin.grpc.learner.SimilarityRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_SIMILARITIES, getCallOptions(), request);
    }
  }

  public static class LearnerFutureStub extends io.grpc.stub.AbstractStub<LearnerFutureStub>
      implements LearnerFutureClient {
    private LearnerFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private LearnerFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LearnerFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new LearnerFutureStub(channel, callOptions);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<br.ufmg.dcc.latin.grpc.learner.LoadReply> load(
        br.ufmg.dcc.latin.grpc.learner.LoadRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LOAD, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<br.ufmg.dcc.latin.grpc.learner.SimilarityResponse> getSimilarities(
        br.ufmg.dcc.latin.grpc.learner.SimilarityRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_SIMILARITIES, getCallOptions()), request);
    }
  }

  private static final int METHODID_LOAD = 0;
  private static final int METHODID_GET_SIMILARITIES = 1;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final Learner serviceImpl;
    private final int methodId;

    public MethodHandlers(Learner serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_LOAD:
          serviceImpl.load((br.ufmg.dcc.latin.grpc.learner.LoadRequest) request,
              (io.grpc.stub.StreamObserver<br.ufmg.dcc.latin.grpc.learner.LoadReply>) responseObserver);
          break;
        case METHODID_GET_SIMILARITIES:
          serviceImpl.getSimilarities((br.ufmg.dcc.latin.grpc.learner.SimilarityRequest) request,
              (io.grpc.stub.StreamObserver<br.ufmg.dcc.latin.grpc.learner.SimilarityResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static io.grpc.ServerServiceDefinition bindService(
      final Learner serviceImpl) {
    return io.grpc.ServerServiceDefinition.builder(SERVICE_NAME)
        .addMethod(
          METHOD_LOAD,
          asyncUnaryCall(
            new MethodHandlers<
              br.ufmg.dcc.latin.grpc.learner.LoadRequest,
              br.ufmg.dcc.latin.grpc.learner.LoadReply>(
                serviceImpl, METHODID_LOAD)))
        .addMethod(
          METHOD_GET_SIMILARITIES,
          asyncUnaryCall(
            new MethodHandlers<
              br.ufmg.dcc.latin.grpc.learner.SimilarityRequest,
              br.ufmg.dcc.latin.grpc.learner.SimilarityResponse>(
                serviceImpl, METHODID_GET_SIMILARITIES)))
        .build();
  }
}
