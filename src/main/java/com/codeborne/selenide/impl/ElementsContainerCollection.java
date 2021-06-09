package com.codeborne.selenide.impl;

import com.codeborne.selenide.*;
import com.codeborne.selenide.ex.ElementNotFound;
import com.codeborne.selenide.ex.PageObjectException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.impl.Plugins.inject;

@ParametersAreNonnullByDefault
public class ElementsContainerCollection<D extends ElementsContainer> extends AbstractList<D> {
  private final WebElementSelector elementSelector = inject(WebElementSelector.class);
  private final PageObjectFactory pageFactory;
  private final Driver driver;
  private final WebElementSource parent;
  private final Field field;
  private final Class<?> listType;
  private final Type[] genericTypes;
  private final By selector;

  public ElementsContainerCollection(PageObjectFactory pageFactory, Driver driver, @Nullable WebElementSource parent,
                                     Field field, Class<?> listType, Type[] genericTypes, By selector) {
    this.pageFactory = pageFactory;
    this.driver = driver;
    this.parent = parent;
    this.field = field;
    this.listType = listType;
    this.genericTypes = genericTypes;
    this.selector = selector;
  }

  @Nonnull
  @CheckReturnValue
  public static List<String> texts(@Nullable Collection<WebElement> elements) {
    return ElementsCollection.texts(elements);
  }

  @CheckReturnValue
  @Nonnull
  @Override
  public D get(int index) {
    WebElementSource self = new ElementFinder(driver, parent, selector, index);
    return initContainer(self);
  }

  @CheckReturnValue
  @Nonnull
  public ElementsCollection self() {
    return new ElementsCollection(getCurrentCollection());
  }

  @Nonnull
  @CheckReturnValue
  public D find(Condition condition) throws ReflectiveOperationException {
    CollectionElementByCondition collectionElementByCondition = new CollectionElementByCondition(getCurrentCollection(), condition);
    WebElementSource self = getConditionalCollection(collectionElementByCondition);
    return initContainer(self);
  }

  @Nonnull
  @CheckReturnValue
  public D findBy(Condition condition) {
    CollectionElementByCondition collectionElementByCondition = new CollectionElementByCondition(getCurrentCollection(), condition);
    WebElementSource self = getConditionalCollection(collectionElementByCondition);
    return initContainer(self);
  }

  @Nonnull
  @CheckReturnValue
  public List<String> texts() {
    return new ElementsCollection(getCurrentCollection()).texts();
  }

  @Nonnull
  @CheckReturnValue
  public D first() {
    return get(0);
  }

  @Nonnull
  @CheckReturnValue
  public D last() {
    LastCollectionElement lastCollectionElement = new LastCollectionElement(getCurrentCollection());
    return initContainer(lastCollectionElement);

  }

  private D initContainer(WebElementSource self) {
    try {
      return (D) pageFactory.initElementsContainer(driver, field, self, listType, genericTypes);
    } catch (ReflectiveOperationException e) {
      throw new PageObjectException("Failed to initialize field " + field, e);
    }
  }

  private ElementFinder getConditionalCollection(CollectionElementByCondition collectionElementByCondition) {
    return new ElementFinder(driver, collectionElementByCondition, selector, 0);
  }


  private BySelectorCollection getCurrentCollection() {
    return new BySelectorCollection(driver, parent, selector);
  }

  @CheckReturnValue
  @Override
  public int size() {
    try {
      return elementSelector.findElements(driver, parent, selector).size();
    } catch (NoSuchElementException e) {
      throw new ElementNotFound(driver, selector.toString(), exist, e);
    }
  }
}
