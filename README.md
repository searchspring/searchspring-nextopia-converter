# Searchspring -> Nextopia Converter

Takes a Searchspring response and turns it into Nextopia XML.
Takes a Nextopia query and turns it into a searchspring query.

## Usage

### Convert a response

```java
import com.searchspring.nextopia.Converter;
// ...
String siteId = "abcd12";
Converter converter = new Converter(siteId);
String nextopiaXml = converter.convertSearchspringResponse(searchspringJson);
```

### Convert a query

```java
import com.searchspring.nextopia.Converter;
// ...
String siteId = "abcd12";
Converter converter = new Converter(siteId);
String searchspringQueryUrl = converter.convertNextopiaQueryUrl(nextopiaQuery);
```
