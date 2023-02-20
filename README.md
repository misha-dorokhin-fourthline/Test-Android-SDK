# Android Fourthline SDK

* [Overview](#overview)
* [Integration](#integration)
* [Security note](#security-note)
    + [Deleting the Temporary Files](#deleting-the-temporary-files)
* [Orca](#orca)
  * [KYC](#kyc)
    + [Test Me](#test-me)
    + [Configuration](#configuration)
        - [Default Flow](#default-flow)
        - [Custom flow](#custom-flow)
    + [KycCompletionBlock](#kyccompletionblock)
      - [Success handling](#success-handling)
      - [Error handling](#error-handling)
    + [List of supported countries](#list-of-supported-countries)
    + [KycInfo](#kycinfo)
      - [Overview](#overview-1)
      - [Mandatory data](#mandatory-data)
      - [`KycInfo` and KYC Product](#kycinfo-and-kyc-product)
    + [Zipper](#zipper)
      - [Error handling](#error-handling-1)
  * [Document](#document)
    + [Overview](#overview-2)
    + [Test Me](#test-me-1)
    + [Configuration](#configuration-1)
    + [DocumentCompletionBlock](#documentcompletionblock)
      * [Success handling](#success-handling-1)
      * [Error handling](#error-handling-2)
  * [Selfie](#selfie)
    + [Overview](#overview-3)
    + [Test Me](#test-me-2)
    + [Configuration](#configuration-2)
    + [SelfieCompletionBlock](#selfiecompletionblock)
      * [Success handling](#success-handling-2)
      * [Error handling](#error-handling-3)      
  * [NFC](#nfc)
    + [Overview](#overview-4)
    + [Configuration](#configuration-3)
      * [MRTD](#mrtd)
      * [IDL](#idl)
    + [NfcCompletionBlock](#nfccompletionblock)
      * [Success handling](#success-handling-3)
      * [Error handling](#error-handling-4)
  * [QES](#qes)
    + [Overview](#overview-5)
    + [Test Me](#test-me-1)
    + [Configuration](#configuration-4)
    + [QesCompletionBlock](#qescompletionblock)
      - [Success handling](#success-handling-4)
      - [Error handling](#error-handling-5)
  * [UI Customization](#ui-customization)
    + [Overview](#overview-6)
    + [Fonts](#fonts)
    + [Colors](#colors)
      - [Color Palette](#color-palette)
      - [Orca Elements](#orca-elements)
      - [Dark Mode](#dark-mode)
    + [Layout Details](#layout-details)
    + [Localization](#localization)
      - [Base Language](#base-language)
      - [Fixed Language](#fixed-language)
  * [NetworkEnvironment](#networkenvironment)
- [Fourthline Analytics](#fourthline-analytics)
  * [Importing Datadog](#importing-datadog)
  * [Enabling Fourthline Analytics](#enabling-fourthline-analytics)
  * [Analytics Tracking Consent](#analytics-tracking-consent)
  * [Debug builds](#debug-builds)
* [Troubleshooting](#troubleshooting)
  * [`kotlin-reflect`](#-kotlin-reflect-)

## Overview
Fourthline SDK contains a drop-in module called Orca. It handles all the flows and UI in order to integrate a full KYC or QES solution in a very short period of time.

It supports a certain level of appearance and behaviour customization.
Orca is fully written in Kotlin but it is also compatible with Java.

Fourthline SDK requires minimum API version 23.

## Integration

You can add Orca module of Fourthline SDK to your project using Gradle.

In your **top level** `build.gradle` file, link to Fourthline's repository:

###### Gradle
```groovy
allprojects {
    repositories {
        //...
        maven {
            url "https://maven.pkg.github.com/Fourthline-com/FourthlineSDK-Android"
            credentials {
                username ""
                password getLocalProperty("github_token")
            }
        }
    }
}
```

###### Gradle KTS
```kotlin
allprojects {
    repositories {
        //...
        maven {
            url = URI("https://maven.pkg.github.com/Fourthline-com/FourthlineSDK-Android")
            credentials {
                username = ""
                password = getLocalProperty("github_token")
            }
        }
    }
}
```

Inside your Github account:
- Go to `Settings` -> `Account security` -> `Enable two-factor authentication`. This is required in order to access Fourthline repository.
- Go to `Settings` -> `Developer Settings` -> `Personal Access Tokens` -> `Generate a new Token`
- Select the following scope: "read:packages" and generate a new token
- Store the access token in a secure, but accessible location, e.g. `local.properties`. Treat this token as a password - do not add it to your version control system and if it has to be shared, use a password manager.

###### local.properties
```
//..
github_token=YOUR_ACCESS_TOKEN
```

In your module level `build.gradle` file, add a dependency to Fourthline SDK:

###### Gradle
```groovy
dependencies {
  // ...
  def fourthlineSdkVersion = "%%LATEST_SDK_VERSION%%"
  implementation "com.fourthline:fourthline-sdk:$fourthlineSdkVersion"
}
```

###### Gradle KTS
```kotlin
dependencies {
  // ...
  val fourthlineSdkVersion = "%%LATEST_SDK_VERSION%%"
  implementation("com.fourthline:fourthline-sdk:$fourthlineSdkVersion")
}
```

## Security note
#### Deleting the Temporary Files
The SDK stores certain information pertaining to the KYC process inside the application cache folder.<br/>
Information such as the video recordings taken during the Selfie and Document scanners or the KYC zip files.<br/>
This is due to the fact that the video recordings must remain available until they are collected by the KYC module, packaged into the zip file and submitted to the backend.

**It is your responsibility to delete these temporary files when they are no longer needed.**<br/>
For example, right before the app is terminated, in `onDestroy()` or after successfully uploading the KYC zip file to the backend for verification.<br/>

The SDK temporary files are deleted by calling an extension function on `Context`:

###### Kotlin
```kotlin
context.deleteFourthlineFiles()
```

###### Java
```java
FourthlineFileManager.deleteFourthlineFiles(context);
```

## Orca
Orca supports several solutions for your application:
* [KYC](#kyc) (Know Your Customer)
* [QES](#qes) (Qualified Electronic Signature)
* [Selfie](#selfie) (Selfie Scanner with UI)
* [Document](#document) (Document Scanner with UI)
* [NFC](#nfc) (NFC Scanner with UI)

### KYC

There are a few different ways in which KYC Product can be integrated in the app.

#### Test Me
- This is the quickest way to see the look and feel of Orca Kyc Product. It launches the [Default flow](#default-flow) using hardcoded [list of supported countries / documents](#list-of-supported-countries).

###### Kotlin
```kotlin
import com.fourthline.orca.Orca
import com.fourthline.orca.kyc.kyc

Orca.kyc(context).testMe()
```

> **Note:**
> This should be used only in **debug** mode, as it does not create or return any KYC object nor does it accept any configuration.
> It throws `IllegalStateException` when it's started not inside of debuggable build.

#### Configuration
##### Default Flow
- KYC Product configured only using the list of supported countries is presenting the Default Flow. Order in which flows are presented to the user are:
 Document including NFC (if both, the device and the scanned document, are supporting it), Selfie, Location, Address, Contact details.

###### Kotlin
```kotlin
import com.fourthline.core.CountryNetworkModel
import com.fourthline.orca.Orca
import com.fourthline.orca.kyc.KycCompletionBlock
import com.fourthline.orca.kyc.KycConfig
import com.fourthline.orca.kyc.kyc

val supportedCountries: List<CountryNetworkModel>
val kycCompletionBlock: KycCompletionBlock

val kycConfig = KycConfig(supportedCountries)

Orca.kyc(context)
    .configure(config = kycConfig)
    .present(kycCompletionBlock)
```

##### Custom flow
- KYC Product has an option to customize presented flows.
- It allows adding, removing and reordering the following flows: Selfie, Document (with option to include/exclude NFC), Location, Address, Contact details.

**Example 1:**
Configure KYC Product to present the following flows: Selfie, Address, Location.

###### Kotlin
```kotlin
import com.fourthline.orca.Orca
import com.fourthline.orca.kyc.Address
import com.fourthline.orca.kyc.KycCompletionBlock
import com.fourthline.orca.kyc.KycConfig
import com.fourthline.orca.kyc.Location
import com.fourthline.orca.kyc.Selfie
import com.fourthline.orca.kyc.kyc

val kycCompletionBlock: KycCompletionBlock

val kycConfig = KycConfig(
    flows = setOf(Selfie, Address, Location)
)

Orca.kyc(context)
    .configure(kycConfig)
    .present(kycCompletionBlock)
```

**Example 2:**
Configure KYC Product to present the following flows: Document excluding NFC and Contact details.

> **Note:**
> The Document flow must be configured using `KycDocumentFlowConfig`.

###### Kotlin
```kotlin
import com.fourthline.core.CountryNetworkModel
import com.fourthline.orca.Orca
import com.fourthline.orca.kyc.Contacts
import com.fourthline.orca.kyc.Document
import com.fourthline.orca.kyc.KycCompletionBlock
import com.fourthline.orca.kyc.KycConfig
import com.fourthline.orca.kyc.KycDocumentFlowConfig
import com.fourthline.orca.kyc.kyc

val supportedCountries: List<CountryNetworkModel>
val kycCompletionBlock: KycCompletionBlock

val kycConfig = KycConfig(
    flows = setOf(
        Document(config = KycDocumentFlowConfig(supportedCountries, includeNfcFlow = false)),
        Contacts,
    )
)

Orca.kyc(context)
    .configure(kycConfig)
    .present(kycCompletionBlock)
```

#### KycCompletionBlock

KYC Product uses a mechanism of callbacks to provide the feedback. The `kotlin.Result` is the output of the Product.
It is invoked at the end of the process right after Orca shuts down (our activity calls `finish()`) and it is invoked only once.
It can be used as following:

###### Kotlin
```kotlin
import com.fourthline.orca.kyc.KycCompletionBlock

KycCompletionBlock { kycResult ->
        kycResult.fold(
            onSuccess = { TODO() },
            onFailure = { TODO() },
        )
    }
```

##### Success handling

When the KYC Product ends successful, the `onSuccess` lambda is invoked and filled [KycInfo](#KycInfo) object is passed inside.

###### Kotlin
```kotlin
import com.fourthline.orca.kyc.KycCompletionBlock

KycCompletionBlock { kycResult ->
        kycResult.fold(
            onSuccess = { kycInfo -> TODO() },
            onFailure = { TODO() },
        )
    }
```

##### Error handling

When the KYC Product fails for any reason, the `onFailure` lambda is invoked and `Throwable` is passed inside.

###### Kotlin
```kotlin
import com.fourthline.orca.kyc.Canceled
import com.fourthline.orca.kyc.DocumentExpired
import com.fourthline.orca.kyc.DocumentTypeInvalid
import com.fourthline.orca.kyc.DocumentTypeNotSupported
import com.fourthline.orca.kyc.IssuingCountryNotSupported
import com.fourthline.orca.kyc.KycCompletionBlock
import com.fourthline.orca.kyc.KycError
import com.fourthline.orca.kyc.NationalityNotSupported
import com.fourthline.orca.kyc.PersonNotAdult
import com.fourthline.orca.kyc.Unexpected

KycCompletionBlock { kycResult ->
        kycResult.fold(
            onSuccess = { TODO() },
            onFailure = { error ->
                when (error) {
                    is KycError -> when (error) {
                        is Canceled -> TODO()
                        is DocumentExpired -> TODO()
                        is DocumentTypeInvalid -> TODO()
                        is DocumentTypeNotSupported -> TODO()
                        is IssuingCountryNotSupported -> TODO()
                        is NationalityNotSupported -> TODO()
                        is PersonNotAdult -> TODO()
                        is Unexpected -> TODO()
                    }
                    else -> TODO("Things went really bad, e.g. VirtualMachineError etc")
                }
            }
        )
    }
```

In the table below you can find possible errors returned by NFC Product:

|            Name            |                                          Description                                           |
|:--------------------------:|:----------------------------------------------------------------------------------------------:|
|         Canceled          |                                     Canceled by end user.                                     |
|      DocumentExpired       |                User attempting to complete the flow with an expired document.                  |
|    DocumentTypeInvalid     |              Scanned document has different MRZ type than selected document type.              |
|  DocumentTypeNotSupported  | Type of the document provided by user is not supported. If you think it's not supposed to happen, check provided [list of supported countries / documents](#list-of-supported-countries).    |
| IssuingCountryNotSupported | Document provided by user has unsupported issuing country. If you think it's not supposed to happen, check provided [list of supported countries / documents](#list-of-supported-countries). |
|  NationalityNotSupported   | Users' nationality is not supported. If you think it's not supposed to happen, check provided [list of supported countries / documents](#list-of-supported-countries).                       |
|       PersonNotAdult       |                         User attempting to complete the flow is underage.                      |
|         Unexpected         | Unexpected error happened. Please provide the message from it to Fourthline for investigation. |

#### List of supported countries
The Document Flow of KYC Product requires list of the supported countries / documents to properly function.
The list is used for filtering out users on Country Selection Screen, Document Type Selection Screen, Nationality Selection Screen and at Document Scanner to run extra checks over the scanned document.

Fourthline has an endpoint that you can call in order to get the list:
`https://{FourthlineBaseURL}/v1/lists/nfcInformation`
This endpoint will return a JSON which can be used to initialize the `CountryNetworkModel` that is needed by KYC Product that includes the Document Flow.

> **Note:**
> Please review our [Developer Documentation](https://dx.fourthline.com/) for more information on how to use our endpoints.

###### Kotlin
```kotlin
import com.fourthline.core.CountryNetworkModel
import com.fourthline.orca.kyc.KycDocumentFlowConfig

val jsonData: String = performRequest()

val models = CountryNetworkModel.create(from = jsonData)
val documentFlowConfig = KycDocumentFlowConfig(
    supportedCountries = models
)
```

#### KycInfo

##### Overview
KYC Product uses `KycInfo` data class to hold KYC information collected during the process. It is also the successful output of the KYC Product.
For more insights about `KycInfo` please refer to Dokka documentation.

The class can later be used to create a zip file in the format supported by backend of Fourthline. Please refer to [Zipper](#zipper) for more information.

##### Mandatory data

KYC Product takes care of filling data retrieved from user only. There are however other information that is required to successfully upload it to the Fourthline backend - Provider data.

This information should be provided to you by Fourthline and you have to add it to `KycInfo` object before [creating ZIP file](#zipper) as following:

###### Kotlin
```kotlin
import com.fourthline.kyc.KycInfo
import com.fourthline.kyc.Provider

val kycInfo: KycInfo

with(kycInfo) {
    provider = Provider().apply {
        clientNumber = "Client number"
        name = "Provider Name"
    }
}
```

##### `KycInfo` and KYC Product

Kyc Product can be configured for the following options:
1. Orca creates the an empty `KycInfo`, fills it during process and returns it in the end.

> **Note:**
> To use this option simply omit providing `KycInfo` to `KycCustomizationConfig`.

2. Orca accepts prefilled `KycInfo`, creates a copy out of it, fills it during process and returns it in the end.

> **Note:**
> To use this option simply provide `KycInfo` to `KycCustomizationConfig`.

It can be useful if your user already performed KYC and you want to update some of the data.
For example you can ask user to provide another selfie as following:

###### Kotlin
```kotlin
import com.fourthline.kyc.KycInfo
import com.fourthline.orca.Orca
import com.fourthline.orca.kyc.KycCompletionBlock
import com.fourthline.orca.kyc.KycConfig
import com.fourthline.orca.kyc.KycCustomizationConfig
import com.fourthline.orca.kyc.Selfie
import com.fourthline.orca.kyc.kyc

val kycCompletionBlock: KycCompletionBlock
val kycInfo: KycInfo

val kycConfig = KycConfig(flows = setOf(Selfie))
val kycCustomizationConfig = KycCustomizationConfig(kycInfo = kycInfo)

Orca.kyc(context)
    .configure(config = kycConfig)
    .customize(config = kycCustomizationConfig)
    .present(kycCompletionBlock)
```

> **Note:**
> * KYC Product overrides provided data with the data collected during the process.
> * ⚠️ &nbsp; When end user scans Dutch Drivers Licence and provided `Person.nationalityCode` to `KycInfo` is not `null`, Nationality screen is not presented to end user. Same logic applies to Personal Details screen when `Person.gender` is not `null`.
> * `KycInfo` has mutable properties. Changing them on your side after starting Orca will have no effect - Orca works with a copy.

#### Zipper
`Zipper` class takes information from [KycInfo](#KycInfo) to create the XML file with it and to later zip it together with images and videos.
In order to create a zip file simply call `createZipFile()` and provide a valid [KycInfo](#KycInfo) and context.
This will return the URI to the created zip file in the cache folder if all goes good or it will fail and throw `ZipperError`.

> **Note:**
> ⚠️ &nbsp;This method may take some time and it may block the thread on which it was called.
> Please make sure you are not calling in on the **Main Thread**.

##### Error handling

Zipping process can fail to due to following reasons:

| `ZipperError`          | Description                       |
|:-----------------------|:----------------------------------|
| KycNotValid            | `KycInfo` object is not valid, please fix validation errors first. See `KycInfo.validate()` documentation. |
| CannotCreateZip        | There was some IO error during zip creation. |
| ZipExceedMaximumSize   | Zip file exceeding max size (200 MB). |
| NotEnoughSpace         | Insufficient disk space available to create the zip file. |

###### Kotlin
```kotlin
import com.fourthline.kyc.KycInfo
import com.fourthline.kyc.zipper.Zipper
import com.fourthline.kyc.zipper.ZipperError
import com.fourthline.kyc.zipper.ZipperError.*

val kycInfo: KycInfo

try {
    val zipUri = Zipper().createZipFile(kycInfo, context)
} catch (error: ZipperError) {
    when (error) {
        is CannotCreateZip -> TODO()
        is KycNotValid -> TODO()
        is NotEnoughSpace -> TODO()
        is ZipExceedMaximumSize -> TODO()
    }
}
```

#### Document
#### Overview
The Document Product allows to launch the Document Scanner screen in isolation. As soon as user closes the screen, the control is returned back to you and the provided completion block is invoked.

#### Test Me
- This is the quickest way to see the look and feel of Orca Document Product. It launches the Product using hardcoded [list of supported countries / documents](#list-of-supported-countries).

###### Kotlin
```kotlin
import com.fourthline.orca.Orca
import com.fourthline.orca.document.document

Orca.document(context).testMe()
```

> **Note:**
> This should be used only in **debug** mode, as it does not return any result nor does it accept any configuration.
> It throws `IllegalStateException` when it's started not inside of debuggable build.

#### Configuration
It requires to configure with a type of a document to scan and the list of supported countries to launch the Document product.

###### Kotlin
```kotlin
import com.fourthline.core.CountryNetworkModel
import com.fourthline.core.DocumentType
import com.fourthline.orca.Orca
import com.fourthline.orca.document.DocumentCompletionBlock
import com.fourthline.orca.document.DocumentConfig
import com.fourthline.orca.document.document

val completionCallback: DocumentCompletionCallback

val documentConfig = DocumentConfig(
    documentType = DocumentType.PASSPORT,
    supportedCountries = CountryNetworkModel.create(supportedCountriesString)
)

Orca.document(context)
    .configure(config = documentConfig)
    .present(completionCallback)
```

#### DocumentCompletionBlock

Document Product uses a mechanism of callbacks to provide the feedback. The `kotlin.Result` is the output of the Product.
It is invoked at the end of the process right after Orca shuts down (our activity calls `finish()`) and it is invoked only once.
It can be used as following:

###### Kotlin
```kotlin
import com.fourthline.orca.document.DocumentCompletionBlock

DocumentCompletionBlock { documentResult ->
        documentResult.fold(
            onSuccess = { TODO() },
            onFailure = { TODO() },
        )
    }
```

##### Success handling

When the flow ends successful, the `onSuccess` lambda is invoked and the result is passed inside.

###### Kotlin
```kotlin
import com.fourthline.orca.document.DocumentCompletionBlock

DocumentCompletionBlock { documentResult ->
        documentResult.fold(
            onSuccess = { result -> TODO() },
            onFailure = { TODO() },
        )
    }
```

##### Error handling

When the flow fails for any reason, the `onFailure` lambda is invoked and `Throwable` is passed inside.

###### Kotlin
```kotlin
import com.fourthline.orca.document.Canceled
import com.fourthline.orca.document.DocumentError
import com.fourthline.orca.document.DocumentExpired
import com.fourthline.orca.document.DocumentTypeInvalid
import com.fourthline.orca.document.DocumentTypeNotSupported
import com.fourthline.orca.document.IssuingCountryNotSupported
import com.fourthline.orca.document.NationalityNotSupported
import com.fourthline.orca.document.PersonNotAdult
import com.fourthline.orca.document.Unexpected

DocumentCompletionBlock { documentResult ->
  documentResult.fold(
            onSuccess = { TODO() },
            onFailure = { error ->
                when (error) {
                    is DocumentError -> when (error) {
                        is Canceled -> TODO()
                        is DocumentExpired -> TODO()
                        is DocumentTypeInvalid -> TODO()
                        is DocumentTypeNotSupported -> TODO()
                        is IssuingCountryNotSupported -> TODO()
                        is NationalityNotSupported -> TODO()
                        is PersonNotAdult -> TODO()
                        is Unexpected -> TODO()
                    }
                    else -> TODO("Things went really bad, e.g. VirtualMachineError etc")
                }
            }
        )
    }
```

In the table below you can find possible errors returned by the Document Product:

|            Name            |                                          Description                                           |
|:--------------------------:|:----------------------------------------------------------------------------------------------:|
|         Canceled          |                                     Canceled by end user.                                     |
|      DocumentExpired       |                                      Document is expired.                                      |
|    DocumentTypeInvalid     |              Scanned document has different MRZ type than selected document type.              |
|  DocumentTypeNotSupported  |                                Document type is not supported.                                 |
| IssuingCountryNotSupported |                               Issuing country is not supported.                                |
|  NationalityNotSupported   |                                 Nationality is not supported.                                  |
|       PersonNotAdult       |                                   Person is not an adult.                                      |
|         Unexpected         | Unexpected error happened. Please provide the message from it to Fourthline for investigation. |

#### Selfie
#### Overview
The Selfie Product allows to launch the Selfie Scanner screen in isolation. As soon as user closes the screen, the control is returned back to you and the provided completion block is invoked.

#### Test Me
- This is the quickest way to see the look and feel of Orca Selfie Product.

###### Kotlin
```kotlin
import com.fourthline.orca.Orca
import com.fourthline.orca.selfie.selfie

Orca.selfie(context).testMe()
```

> **Note:**
> This should be used only in **debug** mode, as it does not return any result nor does it accept any configuration.
> It throws `IllegalStateException` when it's started not inside of debuggable build.

#### Configuration
There is nothing to configure for the Selfie Product.

###### Kotlin
```kotlin
import com.fourthline.orca.Orca
import com.fourthline.orca.selfie.SelfieCompletionBlock
import com.fourthline.orca.selfie.selfie

val completionCallback: SelfieCompletionCallback

Orca.selfie(context)
    .configure()
    .present(completionCallback)
```

#### SelfieCompletionBlock

Selfie Product uses a mechanism of callbacks to provide the feedback. The `kotlin.Result` is the output of the Product.
It is invoked at the end of the process right after Orca shuts down (our activity calls `finish()`) and it is invoked only once.
It can be used as following:

###### Kotlin
```kotlin
import com.fourthline.orca.selfie.SelfieCompletionBlock

SelfieCompletionBlock { selfieResult ->
        selfieResult.fold(
            onSuccess = { TODO() },
            onFailure = { TODO() },
        )
    }
```

##### Success handling

When the flow ends successful, the `onSuccess` lambda is invoked and the result is passed inside.

###### Kotlin
```kotlin
import com.fourthline.orca.selfie.SelfieCompletionBlock

SelfieCompletionBlock { selfieResult ->
        selfieResult.fold(
            onSuccess = { result -> TODO() },
            onFailure = { TODO() },
        )
    }
```

##### Error handling

When the flow fails for any reason, the `onFailure` lambda is invoked and `Throwable` is passed inside.

###### Kotlin
```kotlin
import com.fourthline.orca.selfie.Canceled
import com.fourthline.orca.selfie.SelfieError
import com.fourthline.orca.selfie.Unexpected

SelfieCompletionBlock { selfieResult ->
  selfieResult.fold(
            onSuccess = { TODO() },
            onFailure = { error ->
                when (error) {
                    is SelfieError -> when (error) {
                        is Canceled -> TODO()
                        is Unexpected -> TODO()
                    }
                    else -> TODO("Things went really bad, e.g. VirtualMachineError etc")
                }
            }
        )
    }
```

In the table below you can find possible errors returned by Selfie Product:

|            Name            |                                          Description                                           |
|:--------------------------:|:----------------------------------------------------------------------------------------------:|
|         Canceled          |                                     Canceled by end user.                                     |
|         Unexpected         | Unexpected error happened. Please provide the message from it to Fourthline for investigation. |


### NFC
#### Overview
The NFC Product can only be launched after the document data was successfully retrieved from the user, as the chip can only be unlocked with the information found on the document.

The NFC Product consists only of NFC Scanner Screen. As soon as user closes the screen, the control is returned back to you and the provided completion block is invoked.

#### Configuration
There are several mandatory things that has to be provided in order to start the NFC Product depending on the document.

##### MRTD
MRTD stands for Machine-readable travel document.
Supported documents can be found in `MrtdNfcDocumentType` enum and they are *Passports, ID cards* and *Residence permits*.
In order to unlock chip of one MRTD of the following sets of information is needed:
1) **Document number**, **document expiry date**, **user's birth date**.
This data can be retrieved from `MrtdMrzInfo` that is returned by Document Product when MRTD is scanned or from `KycInfo.document` and `KycInfo.person` that is returned by KYC Product.
Please refer to [KycInfo](#KycInfo) for more information.

###### Kotlin
```kotlin
import com.fourthline.core.mrz.MrtdMrzInfo
import com.fourthline.orca.Orca
import com.fourthline.orca.nfc.MrtdNfcDocumentType
import com.fourthline.orca.nfc.MrtdNfcUnlockKey
import com.fourthline.orca.nfc.NfcCompletionBlock
import com.fourthline.orca.nfc.NfcConfig
import com.fourthline.orca.nfc.nfc

val completionBlock: NfcCompletionBlock
val documentType: MrtdNfcDocumentType
val mrzInfo: MrtdMrzInfo

val key = MrtdNfcUnlockKey(
    documentNumber = mrzInfo.documentNumber,
    expiryDate = mrzInfo.expirationDate,
    birthDate = mrzInfo.birthDate,
)

val nfcConfig = NfcConfig(
    keyToUnlockChip = key,
    documentType = documentType
)

Orca.nfc(context)
    .configure(nfcConfig)
    .present(completionBlock)
```

2) **CAN**.
The Card access number (CAN) is a 6-digit number that can usually be found at the front of the ID cards and Residence permits and at the first page of Passports.

###### Kotlin
```kotlin
import com.fourthline.orca.Orca
import com.fourthline.orca.nfc.MrtdNfcDocumentType
import com.fourthline.orca.nfc.MrtdNfcUnlockKey
import com.fourthline.orca.nfc.NfcCompletionBlock
import com.fourthline.orca.nfc.NfcConfig
import com.fourthline.orca.nfc.nfc

val completionBlock: NfcCompletionBlock
val documentType: MrtdNfcDocumentType

val key = MrtdNfcUnlockKey(
    canNumber = "123456"
)

val nfcConfig = NfcConfig(
    keyToUnlockChip = key,
    documentType = documentType
)

Orca.nfc(context)
    .configure(nfcConfig)
    .present(completionBlock)
```

##### IDL
IDL stands for ISO-compliant driving licence. At the moment only *Dutch Driving licenses* is supported by NFC Product.
In order to unlock chip of IDL the following information is needed: full **MRZ**.
This data can be retrieved from `IdlMrzInfo` that is returned by Document Product, when IDL is scanned.

###### Kotlin
```kotlin
import com.fourthline.core.mrz.IdlMrzInfo
import com.fourthline.orca.Orca
import com.fourthline.orca.nfc.IdlNfcUnlockKey
import com.fourthline.orca.nfc.NfcCompletionBlock
import com.fourthline.orca.nfc.NfcConfig
import com.fourthline.orca.nfc.nfc

val completionBlock: NfcCompletionBlock
val mrzInfo: IdlMrzInfo

val key = IdlNfcUnlockKey(
    mrz = mrzInfo.rawMrz
)

val nfcConfig = NfcConfig(
    keyToUnlockChip = key,
)

Orca.nfc(context)
    .configure(nfcConfig)
    .present(completionBlock)
```

#### NfcCompletionBlock

NFC Product uses mechanism of callbacks to provide the feedback. The `kotlin.Result` is the output of the Product.
It is invoked at the end of the process right after Orca shuts down (our activity calls `finish()`) and it is invoked only once.
It can be used as following:

###### Kotlin
```kotlin
import com.fourthline.orca.nfc.NfcCompletionBlock

NfcCompletionBlock { nfcResult ->
        nfcResult.fold(
            onSuccess = { TODO() },
            onFailure = { TODO() },
        )
    }
```

##### Success handling

When the flow ends successful, the `onSuccess` lambda is invoked and the result is passed inside.

###### Kotlin
```kotlin
import com.fourthline.orca.nfc.NfcCompletionBlock

NfcCompletionBlock { nfcResult ->
        nfcResult.fold(
            onSuccess = { result -> TODO() },
            onFailure = { TODO() },
        )
    }
```

##### Error handling

When the flow fails for any reason, the `onFailure` lambda is invoked and `Throwable` is passed inside.

###### Kotlin
```kotlin
import com.fourthline.orca.nfc.Canceled
import com.fourthline.orca.nfc.NfcCompletionBlock
import com.fourthline.orca.nfc.NfcError
import com.fourthline.orca.nfc.NfcNotSupported
import com.fourthline.orca.nfc.Unexpected
import com.fourthline.orca.nfc.WrongUnlockKey

NfcCompletionBlock { nfcResult ->
        nfcResult.fold(
            onSuccess = { TODO() },
            onFailure = { error ->
                when (error) {
                    is NfcError -> when (error) {
                        is Canceled -> TODO()
                        is NfcNotSupported -> TODO()
                        is WrongUnlockKey -> TODO()
                        is Unexpected -> TODO()
                    }
                    else -> TODO("Things went really bad, e.g. VirtualMachineError etc")
                }
            }
        )
    }
```

In the table below you can find possible errors returned by NFC Product:

| Name | Description |
|:--------------:|:-------------------------------------------------:|
| Canceled             | Canceled by end user.                     |
| NfcNotSupported       | NFC is not supported by user's device.     |
| WrongUnlockKey        | Cannot unlock chip with provided key. Make sure the data used to create a key is correct.      |
| Unexpected            | Unexpected error happened. Please provide the message from it to Fourthline for investigation. |

### QES
#### Overview
The QES (Qualified Electronic Signature) Standalone process enables you to start the QES flow for users that already are successfully identified in the KYC Product.
This means that the QES Standalone can be triggered right after a KYC, but also weeks or months after the KYC.
This gives you the opportunity to let the user sign documents easily with a highly qualified signature, without the need of going through an identification process again.

The flow starts with retrieving user information using the access code provided during configuration from the backend.
The next step is to get the location of the user and to to start the QES process using it on our backend.
The backend then processes the provided information and returns the status. There are several error statuses that are eventually mapped to one of the `QesError`.
Please refer to [Error handling](#error-handling-1) section for more information.
Depending on the status, the user may be be asked to provide Selfie. This is handled by Orca QES by presenting the selfie scanner.
Otherwise, the Agreement(s) and legal (terms and conditions, privacy statements etc.) documents are presented to user for signing.
When the user proceeds with the presented files, backend is notified.
This triggers sending the OTP code to the mobile phone of the user and the screen to input the code is presented.
After user enters the correct OTP code it's send to the backend for validation.
This is the end of the process, so the user is presented with Summary screen.
As soon as user closes the screen, the control is returned back to you and the provided completion block is invoked.

#### Test Me
This is the quickest way to see how Orca QES Product look and feel.
It will show the complete flow (including Selfie) but it will not make any network calls, instead predefined values are used.

###### Kotlin
```kotlin
import com.fourthline.orca.Orca
import com.fourthline.orca.qes.qes

Orca.qes(context).testMe()
```

> **Note:**
> This should be used only in **debug** mode, as it does not perform any network requests nor does it accept any configuration.
> It throws `IllegalStateException` when it's started not inside of debuggable.

#### Configuration
There are several mandatory things that has to be provided in order to start the flow. They are access code and mobile phone.

> **Note:**
> * ⚠️ &nbsp; Mobile phone number must contain the space char and follow the format: +99 1234567890.
> * Please review our [Developer Documentation](https://dx.fourthline.com/) for more information on
    how to create an access code.

Different environment can be used for testing. Please refer to [NetworkEnvironment](#networkenvironment) for more information.

###### Kotlin
```kotlin
import com.fourthline.networking.NetworkEnvironment
import com.fourthline.orca.Orca
import com.fourthline.orca.qes.AccessCodeSource
import com.fourthline.orca.qes.QesCompletionBlock
import com.fourthline.orca.qes.QesConfig
import com.fourthline.orca.qes.qes

val networkEnvironment: NetworkEnvironment
val accessCodeSource: AccessCodeSource = AccessCodeSource.Value("ABCDEF")
val qesConfig = QesConfig(
  networkEnvironment = networkEnvironment,
  accessCodeSource = accessCodeSource,
  mobilePhoneNumber = "+45 13123123",
)
val completionBlock: QesCompletionBlock

Orca.qes(context)
  .configure(qesConfig)
  .present(completionBlock)
```

#### QesCompletionBlock

QES flow uses mechanism of callbacks to provide the feedback. The `kotlin.Result` is the output of the flow.
It is invoked at the end of the process right after Orca shuts down (our activity calls `finish()`) and it is invoked only once.
It can be used as following:

###### Kotlin
```kotlin
import com.fourthline.orca.qes.QesCompletionBlock

QesCompletionBlock { qesResult ->
        qesResult.fold(
            onSuccess = { TODO() },
            onFailure = { TODO() },
        )
    }
```

##### Success handling

When the flow ends successful, the `onSuccess` lambda is invoked.

###### Kotlin
```kotlin
import com.fourthline.orca.qes.QesCompletionBlock

QesCompletionBlock { qesResult ->
        qesResult.fold(
            onSuccess = { TODO() },
            onFailure = { TODO() },
        )
    }
```

##### Error handling

When the flow fails for any reason, the `onFailure` lambda is invoked and `Throwable` is passed inside.

###### Kotlin
```kotlin
import com.fourthline.orca.qes.Canceled
import com.fourthline.orca.qes.InvalidAccessCode
import com.fourthline.orca.qes.KycRequired
import com.fourthline.orca.qes.QesCompletionBlock
import com.fourthline.orca.qes.QesError
import com.fourthline.orca.qes.Rejected
import com.fourthline.orca.qes.TooManyAuthorizationAttempts
import com.fourthline.orca.qes.TooManyResendOtpAttempts
import com.fourthline.orca.qes.Unexpected

QesCompletionBlock { qesResult ->
        qesResult.fold(
            onSuccess = { TODO() },
            onFailure = { error ->
                when (error) {
                    is QesError -> when (error) {
                        is Canceled -> TODO()
                        is InvalidAccessCode -> TODO()
                        is KycRequired -> TODO()
                        is Rejected -> TODO()
                        is TooManyAuthorizationAttempts -> TODO()
                        is TooManyResendOtpAttempts -> TODO()
                        is Unexpected -> TODO()
                    }
                    else -> TODO("Things went really bad, e.g. VirtualMachineError etc")
                }
            }
        )
    }
```

In the table below you can find possible errors returned by QES flow:

| Name | Description |
|:--------------:|:-------------------------------------------------:|
| Canceled                    | User cancelled the flow. |
| Rejected                     | The user was rejected during one of the QES flow steps. |
| InvalidAccessCode            | The provided access code is invalid. Please check that the provided access code is correct or try to regenerate it. |
| KycRequired                  | User is rejected because KYC has to be performed first. Please do KYC for the user, you can use Orca for it. |
| TooManyAuthorizationAttempts | User tried to authorize documents for sign too many times. Our backend forbids it. You can try to perform QES for this client again with the new access code. |
| TooManyResendOtpAttempts     | User tried to request OTP code too many times. Our backend forbids it. You can try to perform QES for this client again with the new access code. |
| Unexpected                   | Unexpected error happened. Please provide the message from it to Fourthline for investigation. |

### UI Customization
#### Overview
All Orca products are customizable to some extend so that you can match your brand guideline.
At this moment we see the most value in allowing you to customize fonts, colors and a little bit of layouting.

#### Fonts
Orca provides the option to customize the fonts in order to match your brand guideline.
The fonts can be changed using the `OrcaFlavor`.

###### Kotlin
```kotlin
import com.fourthline.orca.core.flavor.OrcaFlavor
import com.fourthline.orca.core.flavor.OrcaFonts

val customFlavor = OrcaFlavor(
    fonts = OrcaFonts(
        screenHeader = OrcaFonts.Font.FromFontRes(fontRes = R.font.roboto_medium, size = 20),
        primaryButton = OrcaFonts.Font.FromFile(file = File(...), size = 18),
    )
)

// Usage
import com.fourthline.orca.Orca
import com.fourthline.orca.[product].[Product]CustomizationConfig
import com.fourthline.orca.[product].[product]

val customizationConfig = [Product]CustomizationConfig(flavor = customFlavor)

Orca.[product](context)
  ...
  .customize(config = customizationConfig)
  ...
```

The table below contains a detailed description of fonts that can be customized and where they are used in Orca.

| Name | Description | Default Value |
|:--------------:|:-------------------------------------------------:|:----------------:|
| screenHeader            | The main title for each screen (e.g.“ID document”, “Select issuing country”)                                                        | Roboto-Medium 24              |
| screenTitle            | The title describing each screen (e.g. ”It’s time to scan your document”)                                                        | Roboto-Medium 18              |
| screenMessage            | The subtitle that offers an in-depth description of the screen. (e.g. “With this scan we can make sure that you really are who you say you are and avoid identity theft.”))                                                        | Roboto-Regular 16              |
| primaryButton            | Font used by the primary button. (e.g. “Continue”, “Start”)                                                        | Roboto-Medium 18             |
| secondaryButton            | Font used by the secondary button. (e.g. “< back”)                                                        | Roboto-Medium 14              |
| inputField            | Font used by the input field. (e.g. “Netherlands”)                                                        | Roboto-Medium 18              |
| inputFieldPlaceholder            | Font used by the input field placeholder. (e.g. “search…”)                                                        | Roboto-Medium 18             |
| inputFieldTitle            | Font used by the input field's title/ header. (e.g. “select issuing country”)                                                        | Roboto-Medium 14              |
| inputFieldStatus            | Font used by the input field's error/hint field. (e.g. “date must be in the future”, “ scanned from document”)                                                        | Roboto-Medium 12              |
| scannerInstructionText            | Font used by the scanner instructions. (e.g. “Please tilt your document…”)                                                        | Roboto-Medium 20              |
| confirmationScreenTitle            | Font used by the confirmation title. (e.g. “Does everything look good?”)                                                       | Roboto-Medium 20              |
| confirmationScreenCheckpoints            | Font used by the confirmation screen checkpoints. (e.g. "• Is the text readable?”)                                                        | Roboto-Regular 16            |
| tableElementTitle            | Font used for the title of the table view elements. (e.g. “National ID card”, “Passport”)                                                        | Roboto-Medium 16             |
| tableElementDescription            | Font used for the description of the table view elements. (e.g. “not accepted”)                                                        | Roboto-Regular 14             |
| instructionsLink            | Font used by the instructions button with link. (e.g. “Scan again”, “Leave identity check”)                                                        | Roboto-Medium 14             |
| hintText            | Font used for hint text usually found under the input fields. (e.g. “Please confirm the above fields.”)                                                        | Roboto-Regular 14             |
| popupHeader         | Font used for the header text usually found in the Info popup screen. (e.g. “We’d like to access to your camera”)                         | Roboto-Medium 24      |
| popupMessage            | Font used for the message text usually found in the Info popup screen. (e.g. “We need temporary access to your camera...”)            | Roboto 16             |
| popupTitle            | Font used for the text usually found in the Error popup screen. (e.g. “Scan failed...”)                                                 | Roboto-Medium 18      |

#### Colors
The colors can be changed using the `OrcaFlavor`.

###### Kotlin
```kotlin
import com.fourthline.orca.core.flavor.OrcaColors
import com.fourthline.orca.core.flavor.OrcaColors.OrcaColor
import com.fourthline.orca.core.flavor.OrcaFlavor

val customFlavor = OrcaFlavor(
    colorsLight = OrcaColors.defaultLightColors().apply {
      buttons.primary.backgroundColor = OrcaColor.FromRes(R.color.colorPrimary)
      popup.backgroundColor = OrcaColor.FromInt(0xEEEEEE)
    },
    colorsDark = OrcaColors.defaultDarkColors().apply {
          buttons.primary.backgroundColor = OrcaColor.FromRes(R.color.colorPrimary)
          popup.backgroundColor = OrcaColor.FromInt(0xEEEEEE)
        }
)

// Usage
import com.fourthline.orca.Orca
import com.fourthline.orca.[product].[Product]CustomizationConfig
import com.fourthline.orca.[product].[product]

val customizationConfig = [Product]CustomizationConfig(flavor = customFlavor)

Orca.[product](context)
  ...
  .customize(config = customizationConfig)
  ...
```

Tables below contains a detailed description of colors that can be customized and where they are used in Orca.

##### Color Palette

| Name | Description | Default value Light Mode | Default value Dark Mode |
|:--------------:|:-------------------------------------------------:|:----------------:|:----------------:|
| primary        |  Color used for illustrations and elements that require focus such as buttons / inputs. Main color used in the Orca. Examples: animations, icons, primary button background, secondary button text, second option text primary button, scanner progress, table selectors, scanner checks, links.  |#0091FF |#7FB3FC |
| accent         |  Color used for some UI elements to display choice                                                        |#00E062              | #00E062              |
| dark           |  Color palette dark.  Mainly used for default heading color                                                        |#333333              | #FFFFFF              |
| darkLight      |  Color palette dark light. Mainly used for default text color                                                        |#585858              | #C3CDD8              |
| darkLighter    |  Color palette dark lighter. Mainly used for default alternative and less emphasis text color                                                        |#878787              |#3D5671              |
| darkSoft       |  Color palette dark soft                                                        |#D6D6D6              | #263444              |
| light          |  Color palette light. Mainly used for default background color                                                        |#FFFFFF              |#172637              |
| gray           |  Color palette gray                                                        |#B3B3B3              |#607B93              |

##### Orca Elements

###### OrcaScreen
| Name                    |  Description                                    | Default value Light Mode  | Default value Dark Mode   |
|:-----------------------:|:-----------------------------------------------:|:-------------------------:|:-------------------------:|
| backgroundColor         |  Default background color                       | color palette light       | color palette light       |
| headerColor             |  Color used by the screen header                | color palette dark        | color palette dark        |
| titleColor              |  Color used by the screen title                 | color palette dark        | color palette dark        |
| messageColor            |  Color used by the screen message               | color palette darkLight   | color palette darkLight   |
| dividerColor            |  Color used by the dividers                     | color palette darkLight   | color palette darkLight   |
| tableCells              |  Defines the colors used by the table cells within Orca |  See OrcaCells    |     See OrcaCells         |

###### OrcaCells
| Name               | Description                                                          |  Default values         |
|:------------------:|:--------------------------------------------------------------------:|:-----------------------:|
| cellStyle1         | Defines cell style used by document type screen's document option    |  See [OrcaCell Style 1](#orcacell-style-1)   |
| cellStyle2         | Defines cell style used by qes agreement screen's documents          |  See [OrcaCell Style 2](#orcacell-style-2)   |

###### OrcaCell Style 1
| Name              | Description                              |   Default value Light Mode | Default value Dark Mode |
|:-----------------:|:----------------------------------------:|:--------------------------:|:-----------------------:|
| backgroundColor   |  Color used as a background of the cell  |        white               |#263444                  |
| textColor         |  Color used by the text of the cell      |          #333333           |         white           |
| iconColor         |  Color used by the icon of the cell      |  color palette primary     |  color palette primary  |
| borderColor       |  Color used by the border of the cell    |     clear                  |              clear      |
| dividerColor      |  Color used by the divider of the cell   |            clear           |     clear               |

###### OrcaCell Style 2
| Name              | Description                              |   Default value Light Mode | Default value Dark Mode   |
|:-----------------:|:----------------------------------------:|:--------------------------:|:-------------------------:|
| backgroundColor   |  Color used as a background of the cell  |         white              |         #263444           |
| textColor         |  Color used by the text of the cell      |  color palette primary     |   color palette primary   |
| iconColor         |  Color used by the icon of the cell      |   color palette primary    |   color palette primary   |
| borderColor       |  Color used by the border of the cell    |        black 10%           |           white 10%       |
| dividerColor      |  Color used by the divider of the cell   |        black 10%              |        white 30%       |

###### OrcaCheckbox
| Name           | Description                                  | Default value Light Mode      | Default value Dark Mode   |
|:--------------:|:--------------------------------------------:|:-----------------------------:|:-------------------------:|
| tintColor      |  Color used as a checkbox primary color      | color palette primary         | color palette primary     |
| iconColor       |  Color used by the checkmark icon           | color palette light           | color palette light       |

###### Buttons
| Name                             | Description                                       | Default value Light Mode | Default value Dark Mode |
|:--------------------------------:|:-------------------------------------------------:|:------------------------:|:-----------------------:|
| primary.textColor                | Primary button text color                         | color palette light      | color palette light     |
| primary.backgroundColor          | Primary button background color                   | color palette primary    | color palette primary   |
| primary.borderColor              | Primary button border color                       | black 10%                | black 10%               |
| secondary.textColor              | Secondary button text color                       | color palette primary    | color palette primary   |
| secondary.backgroundColor        | Secondary button background color                 | background color         | background color        |
| secondary.borderColor            | Secondary button border color                     | black 10%                | black 10%               |
| backButtonColor                  | Back button text color                            | color palette primary    | color palette primary   |
| scannerPrimary.textColor         | Primary scanner button text color (Yes/ Take picture) | color palette light  | color palette light     |
| scannerPrimary.backgroundColor   | Primary scanner button background color           | color palette primary    | color palette primary   |
| scannerPrimary.borderColor       | Primary scanner button border color               | black 10%                | black 10%               |
| scannerSecondary.textColor       | Secondary scanner button text color. Used in the Scanner confirmation screen by the "No" button          | color palette primary | white       |
| scannerSecondary.backgroundColor | Secondary scanner button background color. Used in the Scanner confirmation screen by the "No" button    | white | color palette darkLighter   |
| checkbox                         | Color used by the checkbox                         | See [OrcaCheckbox](#orcacheckbox) | See [OrcaCheckbox](#orcacheckbox) |

###### OrcaInputField
| Name                     | Description                                                               | Default value Light mode    | Default value Dark mode    |
|:------------------------:|:-------------------------------------------------------------------------:|:---------------------------:|:-----------------------------:|
| textColor                | Color used by the input field's text                                      | color palette dark          | color palette dark            |
| disabledTextColor        | Color used by the input field's text in disabled state                    | black 20%                   | white 20%                     |
| placeholderColor         | Color used by the input field's placeholder                               | color palette dark 60%      | color palette dark 60%        |
| titleColor               | Color used by the input field title on the top and by the field's border  | color palette darkLight     | color palette darkLight       |
| statusColor              | Color used by the input field hint message below the field                | color palette darkLighter   | color palette darkLighter     |
| errorColor               | Color used by the input field's error message below the field             | color palette danger        | color palette danger          |
| backgroundColor          | Color used for the input field background                                 | background color            | background color              |
| disabledBackgroundColor  | Color used for the input field background in disabled state               | dark 10%                    | white 40%                     |
| borderColor              | Color used by the input field unfocused border                            | color palette gray          | color palette darkLighter     |
| disabledBorderColor      | Color used by the input field border in disabled state                    |#B3B3B3                      | color palette darkLighter     |

###### OrcaScannerConfirmation
| Name | Description | Default value Light mode| Default value Dark mode |
|:------------------:|:-------------------------------------------------:|:----------------:|:----------------:|
| textColor          | Color used to display scanner confirmation text                        | white                  | white                 |
| backgroundColor    | Color used for the background of the scanner confirmation screens      |#333333                |#172230                 |
| bulletList         |  Color used by the bullet list                                         | color palette primary  | color palette primary |


###### OrcaHint
| Name | Description | Default value Light mode| Default value Dark mode |
|:-------------------:|:-----------------------------------------------:|:----------------:|:----------------:|
| textColor           |  Color used to display hint text                | color palette dark     | color palette dark  |
| backgroundColor     |  Color used to for hint background              |#F3F9FF                 |#3E586F              |
| borderColor         |  Color used for the hint component border       | black 10%              | black 10%           |

###### OrcaBox
| Name                  | Description                               | Default value Light Mode       | Default value Dark Mode   |
|:---------------------:|:-----------------------------------------:|:------------------------------:|:-------------------------:|
| backgroundColor       |  Default background color                 |#F7F7F7                         |#121C28                    |
| borderColor           |  Color used by the border of the box      | black 10%                      | white 10%                 |
| titleColor            |  Color used by the title of the box       | color palette darkLight        | color palette darkLight   |

###### OrcaPopup
| Name | Description | Default value |
|:---------------------:|:------------------------------------------------:|:-----------------------------:|
| backgroundColor       | Color used for the background of the popups      | background color              |
| titleColor            | Color used by the title in the popups            | color palette dark            |
| messageColor          | Color used by the message in the popups          | color palette darkLight       |

###### OrcaGraphic
| Name | Description | Default value Light mode| Default value Dark mode|
|:---------------------:|:-------------------------------------------------:|:----------------:|:----------------:|
| backgroundColor       | Color used for the background of the intro flow animations      |#F3F3F3                 | white 10%                |
| primaryColor          | Color used by the animation in the intro screens and popups     | color palette primary  | color palette primary    |

##### Dark Mode
Dark Mode is supported out of the box. It uses the system theme and cannot be forced.
To customize the colors simply provide new colors to replace the default ones to the `OrcaFlavor.colorsDark`.

###### Kotlin
```kotlin
import com.fourthline.orca.core.flavor.OrcaColors
import com.fourthline.orca.core.flavor.OrcaColors.OrcaColor
import com.fourthline.orca.core.flavor.OrcaFlavor

val customFlavor = OrcaFlavor(
    colorsDark = OrcaColors.defaultDarkColors().apply {
      buttons.primary.backgroundColor = OrcaColor.FromRes(R.color.colorPrimary)
      popup.backgroundColor = OrcaColor.FromInt(0xEEEEEE)
    }
)

// Usage
import com.fourthline.orca.Orca
import com.fourthline.orca.[product].[Product]CustomizationConfig
import com.fourthline.orca.[product].[product]

val customizationConfig = [Product]CustomizationConfig(flavor = customFlavor)

Orca.[product](context)
  ...
  .customize(config = customizationConfig)
  ...
```

#### Layout Details
Layout details can be changed using the `OrcaFlavor`.

###### Kotlin
```kotlin
import com.fourthline.orca.core.flavor.OrcaFlavor
import com.fourthline.orca.core.flavor.OrcaLayouts

val customFlavor = OrcaFlavor(
    layouts = OrcaLayouts(primaryButtonCornerRadius = 0) // rectangular buttons
    // layouts = OrcaLayouts(primaryButtonCornerRadius = 8) // buttons with corner radius equal to 8 dp
    // layouts = OrcaLayouts(primaryButtonCornerRadius = OrcaLayouts.Round) // buttons with rounded corners
)

// Usage
import com.fourthline.orca.Orca
import com.fourthline.orca.[product].[Product]CustomizationConfig
import com.fourthline.orca.[product].[product]

val customizationConfig = [Product]CustomizationConfig(flavor = customFlavor)

Orca.[product](context)
  ...
  .customize(config = customizationConfig)
  ...
```

The table below contains a detailed description of elements that can be customized.

| Name | Description |
|:------------------------------------:|:-------------------------------------------:|
| primaryButtonCornerRadius            | Corner Radius used for the primary buttons. |

#### Localization

Orca can be configured in different languages:
- English (default)
- Dutch
- Spanish
- French
- Italian
- German
- Greek
- Romanian
- Polish
- Portuguese

##### Base Language
`OrcaLocalization.baseLanguage` is the default language to be used in case the users' device language is not one of `OrcaLocalization.LanguageType`.
By default Orca falls back to English. It can be changed by updating the `flavor.localization.baseLanguage` property.

###### Kotlin
```kotlin
import com.fourthline.orca.core.flavor.OrcaFlavor
import com.fourthline.orca.core.flavor.OrcaLocalization

val customFlavor = OrcaFlavor(
    localization = OrcaLocalization(baseLanguage = OrcaLocalization.LanguageType.NL)
)

// Usage
import com.fourthline.orca.Orca
import com.fourthline.orca.[product].[Product]CustomizationConfig
import com.fourthline.orca.[product].[product]

val customizationConfig = [Product]CustomizationConfig(flavor = customFlavor)

Orca.[product](context)
  ...
  .customize(config = customizationConfig)
  ...
```

##### Fixed Language
Setting `OrcaLocalization.fixedLanguage` forces Orca to be in that language. It overrides user preferences and `OrcaLocalization.baseLanguage`.

###### Kotlin
```kotlin
import com.fourthline.orca.core.flavor.OrcaFlavor
import com.fourthline.orca.core.flavor.OrcaLocalization

val customFlavor = OrcaFlavor(
    localization = OrcaLocalization(fixedLanguage = OrcaLocalization.LanguageType.ES)
)

// Usage
import com.fourthline.orca.Orca
import com.fourthline.orca.[product].[Product]CustomizationConfig
import com.fourthline.orca.[product].[product]

val customizationConfig = [Product]CustomizationConfig(flavor = customFlavor)

Orca.[product](context)
  ...
  .customize(config = customizationConfig)
  ...
```

### NetworkEnvironment
It is possible to configure Orca to communicate with different environments as following:
* use `NetworkEnvironment.Production` for targeting Production environment, used for production.
* use `NetworkEnvironment.Sandbox` for targeting Sandbox environment, used for development.
* use `NetworkEnvironment.Mock` for testing locally - no request are performed. Currently only happy flow is supported and no errors are returned back (except for `Canceled`).

## Fourthline Analytics

`FourthlineAnalytics` uses [Datadog](https://www.datadoghq.com) to collect and process the Fourthline SDK usage information.
It requires the [Datadog](https://github.com/DataDog/dd-sdk-android) dependency to be available at runtime.

> **Note:**
> Currently Fourthline SDK works only with Datadog version 1.10.0.

### Importing Datadog

###### Gradle
```groovy
dependencies {
    // ...
    implementation "com.datadoghq:dd-sdk-android:1.10.0"
}
```

###### Gradle KTS
```kotlin
dependencies {
    // ...
    implementation("com.datadoghq:dd-sdk-android:1.10.0")
}
```

> **Note:**
> * The Datadog SDK does not support initializing and running multiple instances.
> * If you are already using the Datadog Android SDK in your project, you are not able to make use of Fourthline Analytics.
> * After initializing Fourthline Analytics, please do not attempt to initialize or make use of your own custom Datadog implementation.

### Enabling Fourthline Analytics

Analytics data collection is **disabled by default**, no data is being collected or sent to Fourthline.

Enabling analytics data collection and reporting is a **two-step process**:

1. Initialize analytics by calling `FourthlineAnalytics.initialize(TENANT_ID, context)` when suitable, usually in `Application.onCreate()` using the tenant id provided by Fourthline.

> **Note:**
> `FourthlineAnalytics.initialize` call throws `AnalyticsError`.

Possible throwable types:

| Type                           | Description                       |
|:-------------------------------|:----------------------------------|
| `AnalyticsError.InvalidTenantId`    | Invalid tenant id. Please ensure you are using the analytics tenant id provided by Fourthline. |
| `AnalyticsError.DatadogNotImported` | Datadog dependency is not available at runtime. |

2. Set `TrackingConsent` to `GRANTED` by calling ` FourthlineAnalytics.setTrackingConsent(TrackingConsent.GRANTED)`.

### Analytics Tracking Consent

By default, **each time** `FourthlineAnalytics` is initialized with a valid tenant id, `TrackingConsent` is set to `PENDING`.

`TrackingConsent` can have one of the following values:
- `PENDING` - the SDK starts collecting and batching the data but does not send it to Datadog. The SDK waits for the new tracking consent value to decide what to do with the batched data.
- `GRANTED` - the SDK starts/continues collecting the data and sends it to Datadog.
- `NOT_GRANTED` - no new data is being collected or sent to Datadog. Any data, previously collected while consent was set to `PENDING`, is removed.

To change the tracking consent value after the SDK is initialized, use  `FourthlineAnalytics.setTrackingConsent`. The SDK changes its behavior according to the new value. For example, if the current tracking consent is `PENDING` and it changes to:
- `GRANTED` - the SDK will send all current and future data to Fourthline.
- `NOT_GRANTED` - the SDK will wipe all current data and will not collect any future data.

### Debug builds

`FourthlineAnalytics` is used to capture production analytics data.\
It does not collect or send any analytics data when `ApplicationInfo.flags` has `FLAG_DEBUGGABLE`.

###### Kotlin
```kotlin
try {
    FourthlineAnalytics.initialize(tenantId = "<Id provided by Fourthline>", context = applicationContext)
} catch (e: AnalyticsError) {
    when (e) {
        is AnalyticsError.DatadogNotImported -> TODO()
        is AnalyticsError.InvalidTenantId -> TODO()
    }
}

FourthlineAnalytics.setTrackingConsent(consent = TrackingConsent.GRANTED)
```

###### Java
```java
try {
    FourthlineAnalytics.initialize("<Id provided by Fourthline>", applicationContext);
} catch (AnalyticsError error) {
    // handle exception
}

FourthlineAnalytics.setTrackingConsent(TrackingConsent.GRANTED);
```

## Troubleshooting

### `kotlin-reflect`

If your project uses `kotlin-reflect` it may happen that after proguard step necessary kotlin code will be removed and it will cause application to crash. In order to resolve it you need to add following section to your proguard rules:
###### Proguard
```
-keep class com.fourthline.vision.internal.** { *; }

-keep class kotlin.coroutines.Continuation {
  public protected *;
}

-keepclassmembers @kotlin.Metadata class com.fourthline.vision.internal.** {
  <methods>;
}
```