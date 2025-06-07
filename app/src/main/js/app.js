const { IPC } = BareKit
const Hyperswarm = require('hyperswarm')
const Hypercore = require('hypercore')
const Corestore = require('corestore')
const BlobServer = require('hypercore-blob-server')
const b4a = require('b4a')

let mapLink = null
let ipcBuffer = ''
IPC.setEncoding('utf8')

IPC.on('data', async (chunck) => {
  ipcBuffer += chunck

  let newLineIndex
  while ((newLineIndex = ipcBuffer.indexOf('\n')) !== -1) {
    const line = ipcBuffer.substring(0, newLineIndex).trim() // Get one line
    ipcBuffer = ipcBuffer.substring(newLineIndex + 1) // Remove the line from buffer

    if (line.length === 0) {
      continue // Skip empty lines
    }

    try {
      const message = JSON.parse(line)

      switch (message.action) {
        case 'start':
          await start(message.data['path'])
          break
        case 'requestMapLink':
          await getMapLink()
      }
    } catch (error) {
      console.error('Error handling IPC message: ', error)
    }
  }
})

async function start(documentsPath) {
  const key = b4a.from(
    '1bfbdb63cf530380fc9ad1a731766d597c3915e32187aeecea36d802bda2c51d',
    'hex'
  )
  const store = new Corestore(documentsPath + '/maps')

  console.log(documentsPath)

  const swarm = new Hyperswarm()
  swarm.on('connection', (conn) => {
    store.replicate(conn)
  })
  const server = new BlobServer(store)
  await server.listen()

  const filenameOpts = {
    filename: '/20250512.pmtiles'
  }
  const link = server.getLink(key, filenameOpts)
  mapLink = link
  console.log('link', link)

  const monitor = server.monitor(key, filenameOpts)
  monitor.on('update', () => {
    console.log('monitor', monitor.stats.downloadStats)
  })

  const topic = Hypercore.discoveryKey(key)
  swarm.join(topic)
}

async function getMapLink() {
  if (mapLink) {
    const message = {
      action: 'requestLink',
      data: {
        url: mapLink
      }
    }

    IPC.write(JSON.stringify(message))
  } else {
    console.log('Map link is not ready yet.')
  }
}
