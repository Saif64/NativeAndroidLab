# Appendix A — Kotlin and JVM Interview Depth

Complete this after Day 12, then repeat its interview drills before Day 30. Day 3 was
enough to start Android; this appendix is the language depth expected when Kotlin is
your production tool.

Use local JVM tests as the workbench. Every section requires a passing example, a
controlled compiler/runtime failure, and a short explanation in `LEARNING_LOG.md`.

## 1. Classes, interfaces, objects, and visibility

Learn and exercise:

- primary/secondary constructors and `init`;
- properties versus fields and custom accessors;
- classes final by default; `open`, `abstract`, and interfaces;
- delegation with `by` when behavior should be composed;
- `object` singleton and companion object;
- top-level declarations;
- `private`, `internal`, `protected`, `public` and module boundaries.

Lab: model an `EntryValidator` interface, two implementations, and a repository that
receives one through its constructor. Compare this with inheritance and explain why
composition is sufficient.

Interview drill: why are Kotlin classes/methods final by default, and how does that
affect mocking, extension, and API design?

## 2. Value modeling

Practice:

- data classes and generated `copy`/equality;
- enums for fixed labels with shared behavior;
- sealed interfaces/classes for a closed set of states;
- value classes for domain-safe wrappers where boxing/API constraints are understood;
- `object`/`data object` for singleton cases;
- exhaustive `when`.

Lab: model `SyncResult` so success, retryable failure, and permanent rejection carry
only valid data for their case. Make the compiler reject a missing `when` branch.

Interview drill: sealed hierarchy versus enum versus open interface?

## 3. Equality, identity, and copying

- `==` calls structural equality; `===` tests reference identity.
- data-class `copy` is shallow.
- mutable collections inside an otherwise data class can still mutate shared state.
- hash-based collection keys must have stable equality/hash behavior.

Lab: put an entry in a `HashSet`, mutate a property participating in its hash through
a deliberately mutable model, and observe lookup failure. Restore immutable modeling.

Interview drill: why can a data class still be mutable or unsafe as a map key?

## 4. Generics and variance

Understand:

- type parameters and constraints;
- declaration-site `out` producer and `in` consumer;
- use-site projections;
- star projections;
- type erasure;
- `reified` type parameters in inline functions.

Lab: create read-only `Source<out T>` and write-only `Sink<in T>` examples with a
small type hierarchy. Predict every allowed and rejected assignment before compiling.

Interview drill: explain PECS-like producer/consumer reasoning in Kotlin and why a
`MutableList<Dog>` is not a `MutableList<Animal>`.

## 5. Higher-order, inline, and extension functions

Practice:

- function types, trailing lambdas, receivers, and callable references;
- non-local return behavior;
- `inline`, `noinline`, and `crossinline` only from a measured/API need;
- extension functions resolved statically and unable to override members;
- infix/operator functions used conservatively.

Lab: write a small `retry` higher-order function with an injected action and policy.
Then list why a production network retry also needs cancellation, idempotency, delay,
and error classification.

Interview drill: extension function versus member function, and what does inline
change besides performance folklore?

## 6. Scope functions and delegated properties

Know the receiver/result distinction:

| Function | Context reference | Returns |
|---|---|---|
| `let` | `it` | lambda result |
| `run` | `this` | lambda result |
| `with` | `this` | lambda result |
| `apply` | `this` | receiver |
| `also` | `it` | receiver |

Do not chain scope functions until ownership becomes unreadable.

Practice delegated properties: lazy initialization, observable delegation, interface
delegation, and Compose's `by` state syntax. Understand that delegation calls operator
functions; it is not special state magic.

Lab: refactor one nested nullable block with `let`, then compare with an early return.
Keep whichever is clearer.

Interview drill: `lateinit` versus `lazy`, including lifecycle and failure behavior.

## 7. Collections, sequences, and complexity

- read-only interface is not proof the backing collection never changes;
- immutable replacement fits UDF;
- list/set/map complexity matters;
- eager collection operators allocate intermediate collections;
- `Sequence` can reduce intermediates for a long pipeline but adds overhead and is not
  automatically faster;
- choose `associateBy`, `groupBy`, `fold`, and set operations from intent.

Lab: benchmark a realistic small and large transformation with collection operations
and Sequence. Keep evidence rather than a rule that Sequence is always faster.

Interview drill: remove duplicates while preserving order and state the complexity.

## 8. Exceptions, Result, and resource safety

- Kotlin has unchecked exceptions; callers still need explicit failure contracts.
- use `try` as an expression where clear;
- `use` closes `Closeable` resources;
- do not swallow coroutine cancellation with broad catches;
- Kotlin `Result` has tradeoffs as a public/domain API; sealed failures may model
  domain cases more clearly.

Lab: parse an untrusted entry payload into a validated domain result. Preserve a
diagnostic cause internally while returning a safe application failure.

Interview drill: exception versus nullable result versus sealed error type?

## 9. Java and Android interoperability

Understand enough to read mixed codebases:

- platform types from Java have uncertain nullability;
- SAM conversion and Java listener APIs;
- checked exceptions are not enforced by Kotlin callers;
- `@JvmStatic`, `@JvmOverloads`, `@JvmField`, and file naming only when Java callers need
  a specific surface;
- Java collections can violate Kotlin null/read-only assumptions;
- suspend functions compile to continuation-based JVM methods;
- Kotlin visibility and internal names become JVM bytecode shapes relevant to R8 and
  reflection.

Lab: write a tiny Java helper returning a possibly null unannotated String. Consume it
from Kotlin, observe the platform type risk, then add nullability annotations or a
safe boundary wrapper.

Interview drill: how can Kotlin still throw an NPE without using `!!` directly?

## 10. JVM runtime basics for Android

Know the useful level—not JVM trivia for its own sake:

- stack frames and heap objects;
- references, reachability, and garbage collection;
- Android Runtime executes DEX and uses AOT/JIT/profile-guided compilation;
- object retention is not necessarily a leak; unwanted reachability from a long-lived
  owner is;
- data races and visibility still exist with coroutines/threads;
- immutability and confinement simplify concurrency;
- boxing can occur with generics/value representations;
- reflection affects startup, optimization, and keep rules.

Lab: deliberately retain an Activity reference in an application-lifetime object,
observe with memory tools, explain the reference chain, then remove it. Use a learning
branch and never keep the leak.

Interview drill: explain why garbage collection cannot free an Activity captured by a
live singleton listener.

## Final appendix gate

Without notes, answer:

1. `==` versus `===`.
2. sealed interface versus enum.
3. covariance versus contravariance.
4. `lateinit` versus `lazy`.
5. extension dispatch behavior.
6. shallow `copy` implications.
7. collection versus Sequence tradeoff.
8. platform type risk.
9. cancellation and broad exception catches.
10. how a reference chain creates an Android memory leak.

Official references:

- https://kotlinlang.org/docs/classes.html
- https://kotlinlang.org/docs/sealed-classes.html
- https://kotlinlang.org/docs/generics.html
- https://kotlinlang.org/docs/extensions.html
- https://kotlinlang.org/docs/inline-functions.html
- https://kotlinlang.org/docs/scope-functions.html
- https://kotlinlang.org/docs/delegated-properties.html
- https://kotlinlang.org/docs/java-interop.html
