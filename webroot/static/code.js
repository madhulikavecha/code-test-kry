const listContainer = document.querySelector('#service-list');
let servicesRequest = new Request('/service');
fetch(servicesRequest)
.then(function(response) { return response.json(); })
.then(function(serviceList) {
  serviceList.forEach(service => {
    var li = document.createElement("li");
    li.appendChild(document.createTextNode('URL = '+service.url + ',  Name =  '+ service.name + ',  Status = ' + service.status + ',  Created_at = ' + service.date));
    var delButton =  document.createElement("button");
      delButton.innerHTML = "delete";
      delButton.onclick =  evt => {
         let serviceToDelete = service.url;
         fetch('/service', {
             method: 'delete',
             headers: {
                 'Accept': 'application/json, text/plain, */*',
                 'Content-Type': 'application/json'
             },
             body: JSON.stringify({name:serviceToDelete})
         }).then(res=> location.reload());
     }
    li.appendChild(delButton);
    listContainer.appendChild(li);
  });
});

const saveButton = document.querySelector('#post-service');
saveButton.onclick = evt => {
    let urlName = document.querySelector('#url-name').value;
    let serviceName = document.querySelector('#service-name').value;
    console.log(urlName+" "+serviceName);
    fetch('/service', {
    method: 'post',
    headers: {
    'Accept': 'application/json, text/plain, */*',
    'Content-Type': 'application/json'
    },
  body: JSON.stringify({url:urlName, name:serviceName})
}).then(res=> location.reload());
}

