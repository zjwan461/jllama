import {Message} from 'element-ui'

export function copy(value) {
  const input = document.createElement('input')
  input.setAttribute('readonly', 'readonly')
  input.setAttribute('value', value)
  document.body.appendChild(input)
  input.setSelectionRange(0, 9999)
  input.select()
  document.execCommand('copy')
  document.body.removeChild(input)
  Message({
    message: '已拷贝',
    type: 'success'
  })
}

export function getRequestBodyStr(obj) {
  let reqStr = ''
  for (let key in obj) {
    reqStr += key + '=' + obj[key] + '&'
  }
  return reqStr
}

export function getRequestBodyJson(obj) {
  return JSON.stringify(obj)
}

export async function fetchFluxData(uri, callback, signal) {
  try {
    const response = await fetch(uri, {signal});
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    // console.log(response)
    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    while (true) {
      const {done, value} = await reader.read()
      if (done) {
        break
      }
      const chunk = decoder.decode(value);
      const data = chunk.replace('data:', '')
      if (callback) {
        callback(data);
      }
    }
  } catch (error) {
    console.error('Error fetching data:', error)
  }
}

export function getSettings() {
  return JSON.parse(sessionStorage.getItem("settings"))
}
