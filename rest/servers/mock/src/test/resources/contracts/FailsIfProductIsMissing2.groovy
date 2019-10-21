package contracts

org.springframework.cloud.contract.spec.Contract.make {
  request {
    method 'POST'
    url '/heartbeat'
    body([
        id: 'my.id',
        name: 'my system',
        organization: 'my organization',
        contact([
            email: 'myemail@myorg.com',
            name: 'my name'
        ]),
        version: "my version",
        url: "https://localhost/uri"
    ])
    headers:
      Content - Type : application / json
      Accept - Version : 1.0.2
    response:
    status: 400
    body:
    status: 400
    path: "/heartbeat"
    headers:
      ontent - Type : application / json
  }