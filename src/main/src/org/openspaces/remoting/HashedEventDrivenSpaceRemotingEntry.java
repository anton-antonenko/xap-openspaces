package org.openspaces.remoting;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author kimchy (shay.banon)
 */
public class HashedEventDrivenSpaceRemotingEntry extends EventDrivenSpaceRemotingEntry {

    public RemotingUtils.MethodHash methodHash;

    public HashedEventDrivenSpaceRemotingEntry buildInvocation(String lookupName, String methodName, RemotingUtils.MethodHash methodHash, Object[] arguments) {
        this.result = null;
        this.ex = null;
        this.isInvocation = Boolean.TRUE;
        this.lookupName = lookupName;
        this.methodName = methodName;
        this.methodHash = methodHash;
        this.arguments = arguments;
        return this;
    }

    @Override
    public EventDrivenSpaceRemotingEntry buildResultTemplate() {
        methodHash = null;
        return super.buildResultTemplate();
    }

    @Override
    public EventDrivenSpaceRemotingEntry buildResult(Throwable e) {
        methodHash = null;
        return super.buildResult(e);
    }

    @Override
    public HashedEventDrivenSpaceRemotingEntry buildResult(Object result) {
        methodHash = null;
        return (HashedEventDrivenSpaceRemotingEntry) super.buildResult(result);
    }


    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        if (in.readBoolean()) {
            methodHash = new RemotingUtils.MethodHash();
            methodHash.readExternal(in);
        }
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        if (methodHash == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            methodHash.writeExternal(out);
        }
    }


}
