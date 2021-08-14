# Searchspring -> Nextopia Converter

Takes a Searchspring response and turns it into Nextopia XML.
Takes a Nextopia query and turns it into a searchspring query.

## Usage

### Convert a response

```java
import com.searchspring.nextopia.Converter;
// ...
Converter converter = new Converter();
String nextopiaXml = converter.convertSearchspringResponse(searchspringJson);
```

### Convert a query

```java
import com.searchspring.nextopia.Converter;
// ...
Converter converter = new Converter();
String searchspringQueryUrl = converter.convertNextopiaQueryUrl(nextopiaQuery);
```
