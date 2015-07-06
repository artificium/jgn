# Java's Future Class #

The following is all from the javadocs at http://java.sun.com/j2se/1.5.0/docs/api/java/util/concurrent/Future.html.


java.util.concurrent
Interface Future

&lt;V&gt;



Type Parameters:
> V - The result type returned by this Future's get method

All Known Subinterfaces:
> ScheduledFuture

&lt;V&gt;



All Known Implementing Classes:
> FutureTask

public interface Future

&lt;V&gt;



A Future represents the result of an asynchronous computation. Methods are provided to check if the computation is complete, to wait for its completion, and to retrieve the result of the computation. The result can only be retrieved using method get when the computation has completed, blocking if necessary until it is ready. Cancellation is performed by the cancel method. Additional methods are provided to determine if the task completed normally or was cancelled. Once a computation has completed, the computation cannot be cancelled. If you would like to use a Future for the sake of cancellability but not provide a usable result, you can declare types of the form Future<?> and return null as a result of the underlying task.

Sample Usage (Note that the following classes are all made-up.)

> interface ArchiveSearcher { String search(String target); }
> class App {
> > ExecutorService executor = ...
> > ArchiveSearcher searcher = ...
> > void showSearch(final String target) throws InterruptedException {
> > > Future

&lt;String&gt;

 future = executor.submit(new Callable

&lt;String&gt;

() {
> > > > public String call() { return searcher.search(target); }

> > > });
> > > displayOtherThings(); // do other things while searching
> > > try {
> > > > displayText(future.get()); // use future

> > > } catch (ExecutionException ex) { cleanup(); return; }

> > }

> }


The FutureTask class is an implementation of Future that implements Runnable, and so may be executed by an Executor. For example, the above construction with submit could be replaced by:

> FutureTask

&lt;String&gt;

 future =
> > new FutureTask

&lt;String&gt;

(new Callable

&lt;String&gt;

() {
> > > public String call() {
> > > > return searcher.search(target);

> > }});

> executor.execute(future);


Since:
  1. 5
See Also:
> FutureTask, Executor