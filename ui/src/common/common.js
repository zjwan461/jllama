import { Message } from 'element-ui'

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
