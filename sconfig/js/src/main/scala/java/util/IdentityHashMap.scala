package java.util

import scala.jdk.CollectionConverters._

class IdentityHashMap[K, V](inner: HashMap[IdentityBox[K], V])
    extends AbstractMap[K, V]
    with Map[K, V]
    with Serializable
    with Cloneable { self =>

  def this() =
    this(new HashMap[IdentityBox[K], V])

  override def clear(): Unit =
    inner.clear()

  override def clone(): AnyRef = {
    new IdentityHashMap(inner.clone().asInstanceOf[HashMap[IdentityBox[K], V]])
  }

  override def containsKey(key: Any): Boolean =
    inner.containsKey(IdentityBox(key.asInstanceOf[K]))

  override def containsValue(value: Any): Boolean =
    inner.containsValue(value.asInstanceOf[V])

  override def entrySet(): Set[Map.Entry[K, V]] =
    inner
      .entrySet()
      .asScala
      .map(e =>
        new AbstractMap.SimpleEntry(e.getKey.apply(), e.getValue)
          .asInstanceOf[Map.Entry[K, V]])
      .asJava

  override def get(key: Any): V =
    inner.get(IdentityBox(key.asInstanceOf[K]))

  override def isEmpty(): Boolean =
    inner.isEmpty

  override def put(key: K, value: V): V =
    inner.put(IdentityBox(key), value)

  override def remove(key: Any): V =
    inner.remove(IdentityBox(key.asInstanceOf[K]))

  override def size(): Int =
    inner.size

  override def values(): Collection[V] =
    inner.values()
}
