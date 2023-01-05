package com.example.pattern.chain;

/**
 * @author gavin.wang
 * @date 2021/12/23 11:28
 * @description TradeHandler
 */
public abstract class TradeHandler<T> {

    protected TradeHandler next;

    public void next(TradeHandler next) {
        this.next = next;
    }

    public abstract void doHandler(TradeHandlerContext context);

    public static class Builder<T> {
        private TradeHandler<T> head;

        private TradeHandler<T> tail;

        public Builder<T> addHandler(TradeHandler handler) {
            if (this.head == null) {
                this.head = this.tail = handler;
                return this;
            }
            this.tail.next(handler);
            this.tail = handler;
            return this;
        }

        public TradeHandler<T> build() {
            return this.head;
        }
    }
}
