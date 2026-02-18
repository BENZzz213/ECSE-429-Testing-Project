#### Session 1 - Core CRUD + Return Codes



##### Todos



* **GET /todos:**



returns 200 + returns 2 todos instances (initial values)

{

    "todos": \[

        {

            "id": "2",

            "title": "file paperwork",

            "doneStatus": "false",

            "description": "",

            "tasksof": \[

                {

                    "id": "1"

                }

            ]

        },

        {

            "id": "1",

            "title": "scan paperwork",

            "doneStatus": "false",

            "description": "",

            "categories": \[

                {

                    "id": "1"

                }

            ],

            "tasksof": \[

                {

                    "id": "1"

                }

            ]

        }

    ]

}



* **POST /todos:**



created a todo with valid input, got 201 (used a Post-response script to save the latest created ID in the environment variable todoId). Recieved the created todo in the body:



{

    "id": "4",

    "title": "Exploratory Task",

    "doneStatus": "false",

    "description": "Testing CRUD"

}



Was able to create a todo with categories and taskof relationship:

{

    "id": "6",

    "title": "Exploratory Task",

    "doneStatus": "false",

    "description": "Testing CRUD",

    "categories": \[

        {

            "id": "1"

        }

    ],

    "tasksof": \[

        {

            "id": "1"

        }

    ]

}



Endpoint returns  400 Bad request when the input has an issue with format to type:



{

    "errorMessages": \[

        "title : field is mandatory"

    ]

}



{"errorMessages":\["com.google.gson.stream.MalformedJsonException: Unterminated object at line 4 column 4 path $."]}



* **PUT /todos:**



returned 405, didn't take any body input. Method not allowed



* **DELETE /todos:**



returned 405 not allowed. tried to test with an input param (id) and returned the same response



* **OPTIONS /todos:**



returned 200 OK. I was expecting a body, but received the info in the header

Allow

OPTIONS, GET, HEAD, POST



* **HEAD /todos:**



returned 200, didn't return a body, but a header. Used to make sure resource exists and validate header values (content type = app/json)



* **PATCH /todos:**



returned 405 method not allowed. known bc PATCH wasn't part of the OPTIONS response





##### Todo By ID



* **GET /todos/:id :**



Returned 200 OK using the following url:

{{baseUrl}}/todos/{{todoId}}



(todoId being the lastly created todo id saved as an env variable to help with exploration)



Tested with an unexisting ID and returned 404 not found
{

    "errorMessages": \[

        "Could not find an instance with todos/5"

    ]

}



* **PUT /todos/:id :**



Returned 200 OK using the same URL as GET. Updated the lastly created todo and got the following response:

{

    "id": "8",

    "title": "Updated Task (PUT)",

    "doneStatus": "true",

    "description": "Updated via PUT"

}



404 returned when id does not exist (tested with id=5)
{

    "errorMessages": \[

        "Invalid GUID for 5 entity todo"

    ]

}



can update title and description but not doneStatus and returns 200:

Request:
{

  "title": "Updated Task (PUT)",

  "description": "Updated via PUT"

}

Response:
{

    "id": "8",

    "title": "Updated Task (PUT)",

    "doneStatus": "false",

    "description": "Updated via PUT"

}



Saw that when doneStatus is set to true, then a PUT request is sent without giving a value to doneStatus, it is set to false... not sure if its a bug or an intended functionality



Mandatory fields have to be added, otherwise 400 Bad Request

{

    "errorMessages": \[

        "title : field is mandatory"

    ]

}



* **POST /todos/:id :**



Returns 200OK, does the same as the todo endpoint, but can be used to update one or more fields even by discarding the mandatory fields in the request body.



{

    "id": "8",

    "title": "Amended via POST",

    "doneStatus": "true",

    "description": "Amended via POST"

}



404 Not found when id does not exist:
{

    "errorMessages": \[

        "No such todo entity instance with GUID or ID 5 found"

    ]

}



* **DELETE /todos/:id :**



returns 200OK. I verified with the get and it actually removes the todo. Does not return body response.



Returned 404 Not Found after sending the request on the id of the same todo I deleted:

{

    "errorMessages": \[

        "Could not find any instances with todos/8"

    ]

}



* **PATCH /todos/:id :**



returns 405 not allowed



* **OPTIONS /todos/:id :**



returns 200OK with all allowed options in the header:

Allow

OPTIONS, GET, HEAD, POST, PUT, DELETE



* **HEAD /todos:**



returned 200, didn't return a body, but a header. Used to make sure resource exists and validate header values (content type = app/json)



#### Session 2 – Relationships + Side Effects





##### Todo: categories relationship



* **GET /todos/:id/categories :**



for available todo ids, the endpoint works as expected and returns 200OK



* **POST /todos/:id/categories:**



Endpoint returns 200OK. Mutliple categories can be assigned to the same todo without overwriting each other.

{

    "todos": \[

        {

            "id": "2",

            "title": "file paperwork",

            "doneStatus": "false",

            "description": "",

            "categories": \[

                {

                    "id": "2"

                },

                {

                    "id": "1"

                }

            ],

            "tasksof": \[

                {

                    "id": "1"

                }

            ]

        }

    ]


We can send the request with id and any other field than id and still get a 201 Created:
{

  "id": "1",

  "title": "test"

}



* **GET/POST /todos/:id/categories/:id**



Documentation says they returns 405, but they returns 404



* **PUT /todos/:id/categories:**



method not allowed (405)



* **DELETE /todos/:id/categories:**



method not allowed (405)



* **PATCH /todos/:id/categories:**



method not allowed (405)



* **OPTIONS /todos/:id/categories:**



Returns 200OK and the allowed enpoints in the Allow section of the header

Allow

OPTIONS, GET, HEAD, POST



* **HEAD /todos/:id/categories:**



Returns 200 OK



* **DELETE POST /todos/:id/categories/:id :**



Deletes successfully when todo and category exist and are linked, then returns 200OK.


When category with id is not linked to todo, returns 404 Not Found:
{

    "errorMessages": \[

        "Could not find any instances with todos/2/categories/1"

    ]

}



##### Todo: project relationship



* **GET/POST /todos/:id/taskof/:id**



Supposed to return 405, but returns 404 not found



#### Session 3 – Validation, Edge Cases \& Content Negotiation



##### Todos



* **POST /todos:**


Cant create a todo with other than title, doneStatus and description:



Request body:
{

  "id": 100,

  "title": 3,

  "doneStatus": false,

  "description": 2

}
Response body:
{

    "errorMessages": \[

        "Invalid Creation: Failed Validation: Not allowed to create with id"

    ]

}




bad request when mandatory field is missing (code 400)



{

    "errorMessages": \[

        "title : field is mandatory"

    ]

}


Title and Description can be an int and are transformed to a string (200OK):



request body:
{

  "title": 3,

  "doneStatus": false,

  "description": 2

}
Response body:
{

    "id": "18",

    "title": "3.0",

    "doneStatus": "false",

    "description": "2.0"

}


input type validation happens with doneStatus (400 Bad request when int is used):
{

    "errorMessages": \[

        "Failed Validation: doneStatus should be BOOLEAN"

    ]

}





##### Todo by ID



* **PUT /todos/:id :**


title can be updated with no apparent caracter limit:



{

    "id": "18",

    "title": "Updated Task (PUT) --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------",

    "doneStatus": "true",

    "description": "400000.0"

}


When request body only has title, both doneStatus and description are set to their default values ("" and false respectively)



Request body:
{

  "title": "Updated Task (PUT)"

}



Response body:
{

    "id": "18",

    "title": "Updated Task (PUT)",

    "doneStatus": "false",

    "description": ""

}



title cannot be empty, 400 bad request returned upon sending:
{

  "title": ""

}

response body:
{

    "errorMessages": \[

        "Failed Validation: title : can not be empty"

    ]

}


* **POST /todos/:id :**



BUG - we can post the following:
{

  "id": 100

}
Get a 200OK response, but the todo Id is not updated:
{

    "id": "18",

    "title": "Amended via POST",

    "doneStatus": "false",

    "description": "Amended via POST"

}


When we submit a description as a boolean, it is transformed to a string (same happens with integer)



Request body:
{

  "description": false

}

Response body:
{

    "id": "22",

    "title": "3.0",

    "doneStatus": "false",

    "description": "false"

}


* **DELETE /todos/:id :**



cant delete a todo twice:

{

    "errorMessages": \[

        "Could not find any instances with todos/22"

    ]

}



##### Todo: categories relationship



* **GET /todos/:id/categories**

BUG - When testing with id = 1000 I get a list of duplicated categories even tho todo with id 1000 does not exist:



{{baseUrl}}/todos/1000/categories

{

    "categories": \[

        {

            "id": "1",

            "title": "Office",

            "description": ""

        },

        {

            "id": "1",

            "title": "Office",

            "description": ""

        },

        {

            "id": "1",

            "title": "Office",

            "description": ""

        },

        {

            "id": "1",

            "title": "Office",

            "description": ""

        }

    ]

}



* **POST /todos/:id/categories:**
  id cant be an integer:



{

    "errorMessages": \[

        "Could not find thing matching value for id"

    ]

}



* **DELETE todos/:id/categories**
  No custom handling of cases where we try to delete a category from an unexisting todo id:



{"errorMessages":\["Cannot invoke

\\"uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance.getRelationships()\\" because \\"parent\\" is null"]}



##### Todo: Project relationship



* **GET todos/:id/tasksof**

BUG - When testing with {{baseUrl}}/todos/1000/tasksof where 1000 is a todo id that does not exist, I recieve the response body of project with id 1 duplicated 6 times



{

    "projects": \[

        {

            "id": "1",

            "title": "Office Work",

            "completed": "false",

            "active": "false",

            "description": "",

            "tasks": \[

                {

                    "id": "2"

                },

                {

                    "id": "12"

                },

                {

                    "id": "6"

                },

                {

                    "id": "1"

                },

                {

                    "id": "9"

                },

                {

                    "id": "7"

                }

            ]

        },

        {

            "id": "1",

            "title": "Office Work",

            "completed": "false",

            "active": "false",

            "description": "",

            "tasks": \[

                {

                    "id": "2"

                },

                {

                    "id": "12"

                },

                {

                    "id": "6"

                },

                {

                    "id": "1"

                },

                {

                    "id": "9"

                },

                {

                    "id": "7"

                }

            ]

        },

        {

            "id": "1",

            "title": "Office Work",

            "completed": "false",

            "active": "false",

            "description": "",

            "tasks": \[

                {

                    "id": "2"

                },

                {

                    "id": "12"

                },

                {

                    "id": "6"

                },

                {

                    "id": "1"

                },

                {

                    "id": "9"

                },

                {

                    "id": "7"

                }

            ]

        },

        {

            "id": "1",

            "title": "Office Work",

            "completed": "false",

            "active": "false",

            "description": "",

            "tasks": \[

                {

                    "id": "2"

                },

                {

                    "id": "12"

                },

                {

                    "id": "6"

                },

                {

                    "id": "1"

                },

                {

                    "id": "9"

                },

                {

                    "id": "7"

                }

            ]

        },

        {

            "id": "1",

            "title": "Office Work",

            "completed": "false",

            "active": "false",

            "description": "",

            "tasks": \[

                {

                    "id": "2"

                },

                {

                    "id": "12"

                },

                {

                    "id": "6"

                },

                {

                    "id": "1"

                },

                {

                    "id": "9"

                },

                {

                    "id": "7"

                }

            ]

        },

        {

            "id": "1",

            "title": "Office Work",

            "completed": "false",

            "active": "false",

            "description": "",

            "tasks": \[

                {

                    "id": "2"

                },

                {

                    "id": "12"

                },

                {

                    "id": "6"

                },

                {

                    "id": "1"

                },

                {

                    "id": "9"

                },

                {

                    "id": "7"

                }

            ]

        }

    ]

}




* **DELETE POST /todos/:id/taskof/:id :**

No custom handling of cases where we try to delete a project from an unexisting todo id:



{"errorMessages":\["Cannot invoke

\\"uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance.getRelationships()\\" because \\"parent\\" is null"]}

