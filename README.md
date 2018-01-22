
# Elasticsearch Amazon Rekognition Image Ingest Processor

[Elasticsearch ingest processors](https://www.elastic.co/guide/en/elasticsearch/reference/master/ingest-processors.html) using [Amazon Rekognition](https://aws.amazon.com/rekognition/) for image analysis. All Rekognition detection features are supported via separate processors..

Each field that is sent through the ingest process will result in an AWS Rekognition API call, so this system is not meant for clusters with large workloads. For better performance, your Elasticsearch ingest nodes should not only be hosted in AWS, but should also be in the region used in the AWS Rekognition API (configurable).

Calls to AWS Rekognition are best suited in your ETL pipeline and **not** via a plugin. There are two benefits to running the code within an ingest node
1. Pipelines are configurable, so you can enable/disable processors without changing your ETL code.
2. Your language of choice for indexing is not as fast as Java.

## [AWS Rekognition Pricing](https://aws.amazon.com/rekognition/pricing/) (most regions)

| Image Analysis Tiers | Price per 1,000 Images Processed |
| ---- | ---- |
| First 1 million images processed* per month  | $1.00 |
| Next 9 million images processed* per month   | $0.80 |
| Next 90 million images processed* per mont   | $0.60 |
| Over 100 million images processed* per month | $0.40 |

## Supported Features
* [Detecting Objects and Scenes](https://docs.aws.amazon.com/rekognition/latest/dg/labels.html  )
* [Detecting Celebrities](https://docs.aws.amazon.com/rekognition/latest/dg/celebrities.html)
* [Detecting Text](https://docs.aws.amazon.com/rekognition/latest/dg/text-detection.html)
* [Detecting Unsafe Content](https://docs.aws.amazon.com/rekognition/latest/dg/moderation.html)

## Building

There is no downloadable version of the plugin for two reasons:

1. It is difficult to release a plugin for each minor version of Elasticsearch. You can only run plugins built for the exact version of Elasticsearch.
2. Due to the warning at the very top regarding cost and performance, it prefered that the plugin is built and not blindly installed so that users are aware. 

Only Elasticsearch 5.6+ is supported in order to take advantage of the secure keystore.

Integration tests are only run if AWS credentials are added to build.gradle. Results are subject to change based on Rekognition's results at the time of the tests.

## Installation

Only basic credentials are supported. The AWS access and secret keys are added to [Elasticsearch keystore](https://www.elastic.co/guide/en/elasticsearch/reference/current/secure-settings.html), before the node is started.

## Plugin Settings
| Setting | Description |
| ---- | ---- |
| ingest.aws-rekognition.credentials.access_key | AWS Acesss key |
| ingest.aws-rekognition.credentials.secret_key | AWS Secret Key |
| ingest.aws-rekognition.region | AWS region used to the API call. Default region is us-east-1 |

AWS Credentials are not configured in elasticsearch.yml, or in the plugin settings, but in the keystore. Settings must be in place before Elasticsearch is started.

## Processor settings

| Name | Required | Default | Description |
| ---- | ---- | ---- | ---- |
| field | yes | - | The field to analyze |
| target_field | no | A new field with the name of the source field with a processor specific suffix appended | The field to assign the converted value to. |
| min_score | no | 0 (all returned) | The minimum confidence score threshold of values to be returned |
| max_values | no | 0 (all returned) | The number of values to return.  If max_value is 1, a single value is returned and not an array. Not used in the Detect Celebrities processor. |
| ignore_missing | no | false | If true and field does not exist or is null, the processor quietly exits without modifying the document |
| remove | no | true | If true, removes the source field after processing. Recommended since storing binary data in Elasticsearch is not ideal. |

| Feature | Processor Name | Default suffix |
| ---- | ---- | --- |
| Detecting Objects and Scenes | detect-objects | _objects |
| Detecting Celebrities | detect-celebrities | _celebrities |
| Detecting Text | detect-unsafe-content | _text |
| Detecting Unsafe Content | detect-dominant-language | _unsafe |

## Examples

After each pipeline is configured, the same document is indexed.

Base64 content is too large to display here and for normal curl/sense usage. Create a JSON file with the required field.
```
{
  "my_field" : "/9j/4gIcSUNDX1BST0.....<insert base64 encoded here, see image.base64.txt>"
}
```

Add the document
```
curl -XPUT $ES_HOST:9200/my-index/my-type/1?pipeline=aws-rekognition-pipeline -d @doc.json
```

Detecting Objects
```
PUT _ingest/pipeline/aws-rekognition-pipeline
{
   "description": "A pipeline to test AWS Rekognition",
   "processors": [
      {
         "detect-objects": {
            "field": "my_field"
         }
      }
   ]
}
```

Result
```
{
   "_index": "my-index",
   "_type": "my-type",
   "_id": "1",
   "_version": 1,
   "found": true,
   "_source": {
      "my_field_objects": [
         "Human",
         "People",
         "Person",
         "Poster",
         "Brochure",
         "Flyer",
         "Paper",
         "Collage",
         "Art",
         "Head"
      ]
   }
}
```

Detecting Celebrities
```
PUT _ingest/pipeline/aws-rekognition-pipeline
{
   "description": "A pipeline to test AWS Rekognition",
   "processors": [
      {
         "detect-celebrities": {
            "field": "my_field"
         }
      }
   ]
}
```

Result
```
{
   "_index": "my-index",
   "_type": "my-type",
   "_id": "1",
   "_version": 1,
   "found": true,
   "_source": {
      "my_field_celebrities": {
         "unknownFaces": 1,
         "celebrityFaces": [
            {
               "name": "Elvis Presley",
               "id": "tX3Fw0h"
            }
         ]
      }
   }
}
```

Detecting Text
```
PUT _ingest/pipeline/aws-rekognition-pipeline
{
   "description": "A pipeline to test AWS Rekognition",
   "processors": [
      {
         "detect-text": {
            "field": "my_field"
         }
      }
   ]
}
```

Result
```
{
   "_index": "my-index",
   "_type": "my-type",
   "_id": "1",
   "_version": 1,
   "found": true,
   "_source": {
      "my_field_text": [
         "PARAMOUNT PRESENTS ELVIS",
         "PRESLEY",
         "\"HAL WALLIS",
         "ING",
         "RELE",
         "CAROLYN JONES .WALTER MATTHAU DOLORES HART. .DEAN JAGGER-VIC MORROW",
         "PAUL STEWART VINCENTE GAZZO",
         "DIRECTED BY MICHAEL CURTIZ SCREENPLAY YBY HERBERT RT BAKER AND MICHAEL",
         "PARAMOUNT",
         "PRESENTS",
         "ELVIS",
         "PRESLEY",
         "\"HAL",
         "WALLIS",
         "ING",
         "RELE",
         "CAROLYN",
         "JONES",
         ".WALTER",
         "MATTHAU",
         "DOLORES",
         "HART.",
         ".DEAN",
         "JAGGER-VIC",
         "MORROW",
         "PAUL STEWART",
         "DIRECTED",
         "BY MICHAEL",
         "CURTIZ",
         "SCREENPLAY",
         "YBY HERBERT",
         "RT BAKER",
         "AND",
         "MICHAEL",
         "VINCENTE GAZZO"
      ]
   }
}
```

Detecting Unsafe Content
```
PUT _ingest/pipeline/aws-rekognition-pipeline
{
   "description": "A pipeline to test AWS Rekognition",
   "processors": [
      {
         "detect-unsafe-content": {
            "field": "my_field"
         }
      }
   ]
}
```

Result
```
{
   "_index": "my-index",
   "_type": "my-type",
   "_id": "1",
   "_version": 1,
   "found": true,
   "_source": {
      "my_field_unsafe": []
   }
}
```

Elvis is safe content!

Using another image with known unsafe content

```
curl -XPUT $ES_HOST:9200/my-index/my-type/2?pipeline=aws-rekognition-pipeline -d @unsafe.json
```

Result
```
{
   "_index": "my-index",
   "_type": "my-type",
   "_id": "2",
   "_version": 1,
   "found": true,
   "_source": {
      "my_field_unsafe": [
         "Explicit Nudity",
         "Nudity"
      ]
   }
}
```